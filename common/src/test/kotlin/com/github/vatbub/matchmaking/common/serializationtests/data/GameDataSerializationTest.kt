/*-
 * #%L
 * matchmaking.common
 * %%
 * Copyright (C) 2016 - 2019 Frederik Kammel
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
package com.github.vatbub.matchmaking.common.serializationtests.data

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.serializationtests.SerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils

class GameDataSerializationTest : SerializationTestSuperclass<GameData>(GameData::class.java) {
    override fun getCloneOf(instance: GameData) = instance.copy()

    override fun newObjectUnderTest(): GameData {
        val gameData = GameData(TestUtils.defaultConnectionId)
        for (i in 1..5) {
            gameData["key$i"] = "value$i"
        }
        // objects are serialized as maps, too
        /*for(i in 1..5){
            gameData["object$i"] = mapOf(Pair("someString", "someString$i"), Pair("someInt", i))
        }*/
        return gameData
    }

    // Already tested in GameDataTest
    override fun notEqualsTest() {}
}
