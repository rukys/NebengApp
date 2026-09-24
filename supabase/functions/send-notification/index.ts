// Supabase Edge Function: send-notification
// Sends push notifications to Android devices via Firebase Cloud Messaging (FCM HTTP v1 API)
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

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

serve(async (req: Request) => {
  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const fcmServerKey = Deno.env.get("FCM_SERVER_KEY") ?? "";

    const supabase = createClient(supabaseUrl, supabaseServiceRoleKey);
    const body: NotificationPayload = await req.json();

    const record = body.record;
    if (!record || !record.user_id) {
      return new Response(JSON.stringify({ error: "Missing record or user_id" }), { status: 400 });
    }

    // 1. Dapatkan FCM token pengguna dari tabel users
    const { data: user, error: userError } = await supabase
      .from("users")
      .select("fcm_token")
      .eq("id", record.user_id)
      .single();

    if (userError || !user?.fcm_token) {
      return new Response(JSON.stringify({ message: "No FCM token for user, skipped" }), { status: 200 });
    }

    // 2. Siapkan channel ID sesuai kategori
    let channelId = "channel_trip_updates";
    if (record.category === "review") {
      channelId = "channel_trip_updates";
    } else if (record.category === "system") {
      channelId = "channel_system";
    }

    // 3. Kirim ke Firebase Cloud Messaging
    const fcmPayload = {
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

    if (fcmServerKey) {
      const fcmResponse = await fetch("https://fcm.googleapis.com/fcm/send", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `key=${fcmServerKey}`,
        },
        body: JSON.stringify(fcmPayload),
      });

      const fcmResult = await fcmResponse.json();
      return new Response(JSON.stringify({ success: true, fcmResult }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ success: true, message: "Logged notification payload" }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (err: unknown) {
    const errorMsg = err instanceof Error ? err.message : String(err);
    return new Response(JSON.stringify({ error: errorMsg }), { status: 500 });
  }
});
