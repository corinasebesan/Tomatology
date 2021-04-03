package com.example.tomatology

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class ExpandableListViewAdapter internal constructor(private val context: Context, private val titleList: List<String>, private val contentList: HashMap<String, List<String>>): BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return titleList.size
    }

    override fun getChildrenCount(p0: Int): Int {
        return this.contentList[this.titleList[p0]]!!.size
    }

    override fun getGroup(p0: Int): Any {
        return titleList[p0]
    }

    override fun getChild(p0: Int, p1: Int): Any {
        return this.contentList[this.titleList[p0]]!![p1]
    }

    override fun getGroupId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getChildId(p0: Int, p1: Int): Long {
        return p1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(p0: Int, p1: Boolean, p2: View?, p3: ViewGroup?): View {
        var p2 = p2
        val titleTile = getGroup(p0) as String

        if(p2 == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            p2 = inflater.inflate(R.layout.list_titles, null)
        }

        val titleTv = p2!!.findViewById<TextView>(R.id.tv_titles)
        titleTv.setText(titleTile)

        return p2
    }

    override fun getChildView(p0: Int, p1: Int, p2: Boolean, p3: View?, p4: ViewGroup?): View {
        var p3 = p3
        val contentTile = getChild(p0,p1) as String

        if(p3 == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            p3 = inflater.inflate(R.layout.list_contents, null)
        }

        val contentTv = p3!!.findViewById<TextView>(R.id.tv_contents)
        contentTv.setText(contentTile)

        return p3
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}