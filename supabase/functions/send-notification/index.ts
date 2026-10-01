// Supabase Edge Function: send-notification
// Sends push notifications to Android devices via Firebase Cloud Messaging (FCM HTTP v1 API)
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";
import { JWT } from "npm:google-auth-library@9.15.1";

interface NotificationPayload {
  type: string;
  table: string;
  record: {
    id: string;
    user_id: string;
    category: string;
    title: string;
    body: string;
    action_url?: string;
  };
}

async function getFirebaseAccessToken(serviceAccount: { client_email: string; private_key: string }): Promise<string> {
  const jwtClient = new JWT({
    email: serviceAccount.client_email,
    key: serviceAccount.private_key,
    scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
  });
  const tokens = await jwtClient.getAccessToken();
  if (!tokens.token) {
    throw new Error("Failed to acquire Firebase OAuth2 token");
  }
  return tokens.token;
}

serve(async (req: Request) => {
  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const serviceAccountRaw = Deno.env.get("FIREBASE_SERVICE_ACCOUNT") 
      ?? Deno.env.get("firebase_service_account") 
      ?? "";
    const fcmServerKey = Deno.env.get("FCM_SERVER_KEY") 
      ?? Deno.env.get("fcm_server_key") 
      ?? "";

    const supabase = createClient(supabaseUrl, supabaseServiceRoleKey);
    const body: NotificationPayload = await req.json();

    const record = body.record;
    if (!record || !record.user_id) {
      return new Response(JSON.stringify({ error: "Missing record or user_id" }), { status: 400 });
    }

    // 1. Ambil FCM token penerima dari tabel users
    const { data: user, error: userError } = await supabase
      .from("users")
      .select("fcm_token")
      .eq("id", record.user_id)
      .single();

    if (userError || !user?.fcm_token) {
      return new Response(JSON.stringify({ message: "No FCM token for user, skipped" }), { status: 200 });
    }

    // 2. Channel notification mapping
    let channelId = "channel_trip_updates";
    if (record.category === "review") {
      channelId = "channel_trip_updates";
    } else if (record.category === "chat") {
      channelId = "channel_chat_messages";
    } else if (record.category === "system") {
      channelId = "channel_system";
    }

    // 3. Opsi A: FCM HTTP v1 API (Menggunakan Service Account JSON - Resmi & Terbaru)
    if (serviceAccountRaw) {
      const serviceAccount = JSON.parse(serviceAccountRaw);
      const accessToken = await getFirebaseAccessToken(serviceAccount);
      const projectId = serviceAccount.project_id;

      const fcmV1Payload = {
        message: {
          token: user.fcm_token,
          notification: {
            title: record.title,
            body: record.body,
          },
          data: {
            category: record.category ?? "trip",
            action_url: record.action_url ?? "",
            channel_id: channelId,
          },
          android: {
            priority: "HIGH",
            notification: {
              channel_id: channelId,
            },
          },
          fcm_options: {
            link: record.action_url ?? "",
          },
        },
      };

      const response = await fetch(
        `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify(fcmV1Payload),
        }
      );

      const result = await response.json();
      return new Response(JSON.stringify({ success: true, api: "v1", result }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    }

    // 4. Opsi B: Fallback Legacy FCM Server Key
    if (fcmServerKey) {
      const fcmLegacyPayload = {
        to: user.fcm_token,
        notification: {
          title: record.title,
          body: record.body,
        },
        data: {
          category: record.category,
          action_url: record.action_url ?? "",
          channel_id: channelId,
        },
        priority: "high",
      };

      const response = await fetch("https://fcm.googleapis.com/fcm/send", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `key=${fcmServerKey}`,
        },
        body: JSON.stringify(fcmLegacyPayload),
      });

      const result = await response.json();
      return new Response(JSON.stringify({ success: true, api: "legacy", result }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ error: "No FIREBASE_SERVICE_ACCOUNT or FCM_SERVER_KEY configured" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  } catch (err: unknown) {
    const errorMsg = err instanceof Error ? err.message : String(err);
    return new Response(JSON.stringify({ error: errorMsg }), { status: 500 });
  }
});
