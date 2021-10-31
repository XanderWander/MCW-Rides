package com.mcw.rides.generic.modules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mcw.rides.Main
import com.mcwalibi.rides.editor.objects.editor.EditorData
import com.mcwalibi.rides.editor.objects.editor.RideList
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

class StorageManager(private val main: com.mcw.rides.Main) {

    private val loadedJson = HashMap<String, String?>()

    private fun getJsonPath(file: String): String {
        return "plugins/${main.name}/${file}.json"
    }

    private fun getJsonFile(file: String): File {
        val jsonFile = File(getJsonPath(file))
        jsonFile.parentFile.mkdirs()
        jsonFile.createNewFile()
        return jsonFile
    }

    private fun getJsonString(file: String): String? {

        val jsonFile = getJsonFile(file)
        val jsonString: String

        try {
            jsonString = jsonFile.bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString

    }

    private fun loadJson(file: String) {
        if (!loadedJson.containsKey(file)) getJsonString(file)?.let { loadedJson.put(file, it) }
    }

    fun saveRide(ride: String, obj: Any?) {
        val gson = Gson()
        File("plugins/${main.name}/$ride").mkdirs()
        val writer = FileWriter(getJsonPath("$ride/data"))
        gson.toJson(obj, writer)
        loadedJson["$ride/data"] = gson.toJson(obj)
        writer.flush()
        writer.close()
    }

    fun saveToJson(file: String, obj: RideList) {

        Bukkit.broadcastMessage("Saving..")

        val gson = Gson()
        File("plugins/${main.name}/$file").mkdirs()
        val writer = FileWriter(getJsonPath(file))
        gson.toJson(obj, writer)
        loadedJson[file] = gson.toJson(obj)
        writer.flush()
        writer.close()

    }

    fun getFromJsonEditorData(file: String): EditorData {

        loadJson(file)
        val json = loadedJson[file]
        val gson = Gson()
        val type = object : TypeToken<EditorData>() {}.type
        var obj: EditorData? = gson.fromJson(json, type)
        if (obj == null) obj = EditorData()
        return obj

    }

    fun getJsonRideList(): RideList {

        loadJson("data")
        val json = loadedJson["data"]
        val gson = Gson()
        val type = object : TypeToken<RideList>() {}.type
        var obj: RideList? = gson.fromJson(json, type)
        if (obj == null) obj = RideList()
        return obj

    }

    fun getJson(any: Type): Any {

        loadJson("data")
        val json = loadedJson["data"]
        val gson = Gson()
        var obj: RideList? = gson.fromJson(json, any)
        if (obj == null) obj = RideList()
        return obj

    }

}