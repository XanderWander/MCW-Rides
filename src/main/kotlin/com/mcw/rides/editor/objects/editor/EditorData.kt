package com.mcw.rides.editor.objects.editor

import com.mcwalibi.rides.editor.objects.track.Section
import com.mcwalibi.rides.editor.objects.train.Train

data class EditorData(
        var editedNodes: List<Int> = mutableListOf(),
        var trainAmount: Int = 0, var trains: List<Train> = mutableListOf(),
        var sectionAmount: Int = 0, var sections: List<Section> = mutableListOf(),
        var editorNodes: HashMap<Int, EditorNode> = HashMap(),
        var selectedNode: Int = 0, var newNode: Int = 1
)