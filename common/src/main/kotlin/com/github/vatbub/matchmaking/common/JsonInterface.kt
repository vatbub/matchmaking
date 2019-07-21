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
package com.github.vatbub.matchmaking.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.Reader
import java.lang.reflect.Type

private val gson by lazy { Gson() }
private val prettyGson by lazy { GsonBuilder().setPrettyPrinting().create()!! }

fun toJson(obj: Any, prettify: Boolean = false): String = if (prettify)
    prettyGson.toJson(obj)
else
    gson.toJson(obj)

fun <T> fromJson(json: String, clazz: Class<T>): T =
        gson.fromJson(json, clazz)

fun <T> fromJson(json: Reader, clazz: Class<T>): T =
        gson.fromJson(json, clazz)

fun <T> fromJson(json: String, type: Type): T =
        gson.fromJson(json, type)

fun <T> fromJson(json: Reader, type: Type): T =
        gson.fromJson(json, type)
