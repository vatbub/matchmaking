/*-
 * #%L
 * matchmaking.server-logic
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.matchmaking.server.logic.roomproviders

import com.github.vatbub.matchmaking.server.logic.roomproviders.data.RoomTransaction

internal fun RoomTransaction.canUserJoinOrAbort(userName: String,
                                                whitelist: List<String>?,
                                                blacklist: List<String>?,
                                                minRoomSize: Int,
                                                maxRoomSize: Int): Boolean {
    /*
     * We have to check again even though we have the filter in place
     * because room.gameStarted might have changed between the call to
     * the filter and the beginning of the transaction (multithreading magic :/ )
     */
    if (this.room.gameStarted) {
        this.abort()
        return false
    }

    if ((this.room.connectedUsers.size + 1) > this.room.maxRoomSize) {
        this.abort()
        return false
    }
    if (this.room.minRoomSize < minRoomSize) {
        this.abort()
        return false
    }
    if (this.room.maxRoomSize > maxRoomSize) {
        this.abort()
        return false
    }

    // check the supplied user list
    if (whitelist != null && this.room.connectedUsers.any { !whitelist.contains(it.userName) }) {
        this.abort()
        return false
    }
    if (blacklist != null && this.room.connectedUsers.any { blacklist.contains(it.userName) }) {
        this.abort()
        return false
    }

    // check the room's user list
    if (this.room.whitelist?.contains(userName) == false) {
        this.abort()
        return false
    }
    if (this.room.blacklist?.contains(userName) == true) {
        this.abort()
        return false
    }

    return true
}
