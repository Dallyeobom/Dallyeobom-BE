package kr.dallyeobom.util

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import kr.dallyeobom.entity.User
import org.springframework.stereotype.Component

@Component
class PushSendService {
    private val firebaseMessaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    // 유저의 FCM 토큰으로 푸시 알림을 보냅니다.
    fun sendPushToUser(
        user: User,
        data: Map<String, String>,
    ) = user.fcmToken?.let { sendPushWithToken(it, data) }

    // 한 유저의 토큰으로 푸시 알림을 보냅니다.
    fun sendPushWithToken(
        token: String,
        data: Map<String, String>,
    ): String {
        val message =
            Message
                .builder()
                .setToken(token)
                .putAllData(data)
                .build()

        return firebaseMessaging.send(message)
    }

    fun sendPushToUsers(
        users: List<User>,
        data: Map<String, String>,
    ): List<String?> {
        val fcmToken =
            users.mapNotNull { user ->
                user.fcmToken
            }
        return sendPushWithMulticast(fcmToken, data)
    }

    // 여러 유저의 토큰으로 푸시 알림을 보냅니다.
    fun sendPushWithMulticast(
        tokens: List<String>,
        data: Map<String, String>,
    ): List<String?> {
        val message =
            MulticastMessage
                .builder()
                .addAllTokens(tokens)
                .putAllData(data)
                .build()
        return firebaseMessaging.sendEachForMulticast(message).responses.map { it.messageId }
    }

    // 특정 토픽에 구독한 유저들에게 푸시 알림을 보냅니다.
    fun sendPushWithTopic(
        topic: String,
        data: Map<String, String>,
    ): String {
        val message =
            Message
                .builder()
                .setTopic(topic)
                .putAllData(data)
                .build()

        return firebaseMessaging.send(message)
    }
}
