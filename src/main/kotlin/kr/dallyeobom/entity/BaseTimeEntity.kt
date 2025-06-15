package kr.dallyeobom.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

// 수정은 불가능하고 생성만 가능한 엔티티가 상속받는 클래스
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    @CreatedDate
    @Column(name = "created_datetime", nullable = false, updatable = false)
    lateinit var createdDateTime: LocalDateTime
        protected set
}

// 수정이 가능한 엔티티가 상속받는 클래스
@MappedSuperclass
abstract class BaseModifiableEntity : BaseTimeEntity() {
    @LastModifiedDate
    @Column(name = "updated_datetime", nullable = false)
    lateinit var updatedDateTime: LocalDateTime
        protected set
}

/*
* Soft Delete를 지원해야하는 엔티티가 상속받는 클래스
* 해당 클래스를 상속받는 엔티티들은 아래와 같은 어노테이션 2개를 붙혀서 사용해야한다
* @SQLDelete(sql = "UPDATE Entity SET DELETED_DATE_TIME = current_timestamp WHERE id = ?")
* @Where(clause = "DELETED_DATE_TIME IS NULL")
* */
@MappedSuperclass
abstract class BaseSoftDeletableEntity : BaseModifiableEntity() {
    @Column(name = "deleted_dateTime", nullable = true)
    var deletedDateTime: LocalDateTime? = null
        protected set

    fun delete() {
        this.deletedDateTime = LocalDateTime.now()
    }
}
