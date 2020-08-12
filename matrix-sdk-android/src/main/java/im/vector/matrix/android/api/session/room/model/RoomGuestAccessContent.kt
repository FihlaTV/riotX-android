/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.matrix.android.api.session.room.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class representing the EventType.STATE_ROOM_GUEST_ACCESS state event content
 * Ref: https://matrix.org/docs/spec/client_server/latest#m-room-guest-access
 */
@JsonClass(generateAdapter = true)
data class RoomGuestAccessContent(
        // Required. Whether guests can join the room. One of: ["can_join", "forbidden"]
        @Json(name = "guest_access") val guestAccess: GuestAccess? = null
)

@JsonClass(generateAdapter = false)
enum class GuestAccess(val value: String) {
    @Json(name = "can_join")
    CanJoin("can_join"),
    @Json(name = "forbidden")
    Forbidden("forbidden")
}
