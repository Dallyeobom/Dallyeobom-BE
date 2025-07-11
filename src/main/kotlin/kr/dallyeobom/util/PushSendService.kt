package kr.dallyeobom.util

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kr.dallyeobom.entity.User
import org.springframework.stereotype.Component

@Component
class PushSendService {
    private val firebaseMessaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    // 유저의 FCM 토큰으로 푸시 알림을 보냅니다.
    fun sendPushToUser(
        user: User,
        data: Map<String, String>,
    ) = user.fcmToken?.let { sendPushWithToken(it, data) } ?: PushSendResult.failureWithoutError()

    // 한 유저의 토큰으로 푸시 알림을 보냅니다.
    fun sendPushWithToken(
        token: String,
        data: Map<String, String>,
    ): PushSendResult {
        val message =
            Message
                .builder()
                .setToken(token)
                .putAllData(data)
                .build()

        return sendPush(message)
    }

    fun sendPushToUsers(
        users: List<User>,
        data: Map<String, String>,
    ) = users.associateWith { user ->
        user.fcmToken?.let { sendPushWithToken(it, data) } ?: PushSendResult.failureWithoutError()
    }

    // 특정 토픽에 구독한 유저들에게 푸시 알림을 보냅니다.
    fun sendPushWithTopic(
        topic: String,
        data: Map<String, String>,
    ): PushSendResult {
        val message =
            Message
                .builder()
                .setTopic(topic)
                .putAllData(data)
                .build()

        return sendPush(message)
    }

    private fun sendPush(message: Message): PushSendResult =
        try {
            PushSendResult.success(firebaseMessaging.send(message))
        } catch (e: Exception) {
            PushSendResult.failure(e)
        }

    data class PushSendResult(
        val success: Boolean,
        val messageId: String? = null,
        val error: Throwable? = null,
    ) {
        companion object {
            fun success(messageId: String) = PushSendResult(true, messageId)

            fun failure(error: Throwable) = PushSendResult(false, error = error)

            fun failureWithoutError() = PushSendResult(false)
        }
    }
}
