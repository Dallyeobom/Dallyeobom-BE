package kr.dallyeobom.controller.common.response

import org.springframework.data.domain.Slice

class SliceResponse<R>(
    val items: List<R>,
    val lastId: Long? = null,
    val hasNext: Boolean,
) {
    companion object {
        fun <R> from(
            slice: Slice<R>,
            lastId: Long?,
        ) = SliceResponse(slice.content, lastId, slice.hasNext())
    }
}
