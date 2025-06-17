package kr.dallyeobom.repository

import io.awspring.cloud.s3.S3Template
import kr.dallyeobom.config.properties.ObjectStorageProperties
import org.springframework.stereotype.Repository
import java.io.InputStream

@Repository
class ObjectStorageRepository(
    private val objectStorageProperties: ObjectStorageProperties,
    private val s3Template: S3Template,
) {
    fun upload(
        path: String,
        key: String,
        stream: InputStream,
    ): String {
        val result = s3Template.upload(objectStorageProperties.bucket, path + key, stream)
        return result.filename
    }

    fun getDownloadUrl(key: String): String = "${objectStorageProperties.cdnUrl}/$key"

    fun delete(key: String) { // 참고로 delete는 실시간 반영되지 않음
        s3Template.deleteObject(objectStorageProperties.bucket, key)
    }

    // WARNING: 이 메소드는 모든 객체를 삭제합니다. 주의해서 사용하세요.
    fun deleteAll() {
        s3Template.listObjects(objectStorageProperties.bucket, "").stream().parallel().map { obj ->
            delete(obj.filename)
        }
    }

    companion object {
        const val COURSE_OVERVIEW_IMAGE_PATH = "course/overview/"
        const val COURSE_IMAGE_PATH = "course/image/"
    }
}
