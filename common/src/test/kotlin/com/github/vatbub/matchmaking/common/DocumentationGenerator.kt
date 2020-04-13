/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.*
import com.github.vatbub.matchmaking.common.responses.*
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset

class DocumentationGenerator {
    companion object {
        private const val connectionId = "79f96ee2"
        private const val password = "3450e711"
        private val room1 = Room("73065963", connectionId, listOf("heykey", "mo-mar"), listOf("leoll"))
        private val room2 = Room("73065964", connectionId, listOf("heykey", "mo-mar"), listOf("leoll"))
        private val room3 = Room("73065965", connectionId, listOf("heykey", "mo-mar"), listOf("leoll"))
        private val room4 = Room("73065966", connectionId, listOf("heykey", "mo-mar"), listOf("leoll"))
        private const val requestId = "12345"

        private val separator = File.separator
        private val templateRegex = Regex("\\{jsonSample}")
        private const val templateString = "```json\n * {jsonSample}\n * ```"
        private val codeSampleRegex =Regex("```json.*```", RegexOption.DOT_MATCHES_ALL)
        private val sourceCharset = Charset.forName("UTF-8")
    }

    private val documentationSamples = listOf(DestroyRoomRequest(connectionId, password, room1.id, requestId),
            DisconnectRequest(connectionId, password, requestId),
            GetConnectionIdRequest(requestId),
            GetRoomDataRequest(connectionId, password, room1.id, requestId),
            JoinOrCreateRoomRequest(connectionId, password, Operation.JoinOrCreateRoom, "vatbub", listOf("heykey", "mo-mar"), listOf("leoll")),
            SendDataToHostRequest(connectionId, password, room1.id, listOf(GameData("vatbub", mutableMapOf("someKey" to "someValue")))),
            StartGameRequest(connectionId, password, room1.id, requestId),
            SubscribeToRoomRequest(connectionId, password, room1.id, requestId),
            UpdateGameStateRequest(connectionId, password, room1.id, GameData("vatbub", mutableMapOf("someKey" to "someValue")), listOf(GameData("leoll", mutableMapOf("someProcessedKey" to "someProcessedValue"))), requestId),
            AuthorizationException("Incorrect password.", connectionId, requestId),
            BadRequestException("IllegalArgumentException, There was something wrong with this request.", connectionId, requestId),
            DestroyRoomResponse(connectionId, true, requestId),
            DisconnectResponse(connectionId, listOf(room1, room2), listOf(room3, room4), requestId),
            GetConnectionIdResponse(connectionId, password, requestId),
            GetRoomDataResponse(connectionId, room1, requestId),
            InternalServerErrorException("ArrayIndexOutOfBoundsException, PLease always report these errors to the project maintainers.", connectionId, requestId),
            JoinOrCreateRoomResponse(connectionId, Result.RoomCreated, room1.id, requestId),
            NotAllowedException("Only the host of a game is allowed to start the game.", connectionId, requestId),
            SubscribeToRoomResponse(connectionId, requestId),
            UnknownConnectionIdException("The specified connection id is not known to the server", connectionId, requestId)
    )

    @Test
    fun generateDocumentationSamples() {
        documentationSamples.forEach { sampleObject ->
            val sampleJson = sampleObject.toJson(true)
            val classFile = File(sampleObject.javaClass.getResource("${sampleObject.javaClass.simpleName}.class").toURI())
            insertCodeSample(sampleJson, getSourceFileFromClassFile(classFile))
        }
    }

    @Test
    fun revertDocumentationChanges() {
        documentationSamples.forEach { sampleObject ->
            val sampleJson = sampleObject.toJson(true)
            val classFile = File(sampleObject.javaClass.getResource("${sampleObject.javaClass.simpleName}.class").toURI())
            removeCodeSample(sampleJson, getSourceFileFromClassFile(classFile))
        }
    }

    private fun getSourceFileFromClassFile(classFile: File): File =
            File(classFile.absolutePath
                    .replace("target${separator}classes", "src${separator}main${separator}kotlin")
                    .replace(".class", ".kt"))

    private fun formatCodeSample(codeSample: String): String =
            codeSample.replace("\n", "\n * ")

    private fun insertCodeSample(codeSample: String, sourceFileLocation: File) {
        logger.info("Replacing template in ${sourceFileLocation.absolutePath} ...")
        val sourceContents = FileUtils.readFileToString(sourceFileLocation, sourceCharset)
        val modifiedContents = sourceContents
                .replace(templateRegex, formatCodeSample(codeSample))
        FileUtils.write(sourceFileLocation, modifiedContents, sourceCharset, false)
    }

    private fun removeCodeSample(codeSample: String, sourceFileLocation:File){
        logger.info("Reverting documentation changes in ${sourceFileLocation.absolutePath} ...")
        val sourceContents = FileUtils.readFileToString(sourceFileLocation, sourceCharset)
        val modifiedContents = sourceContents
                .replace(codeSampleRegex, templateString)
        FileUtils.write(sourceFileLocation, modifiedContents, sourceCharset, false)
    }
}
