package com.example.menu.adapter

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.example.menu.R

class MySpinnerAdapter(context: Context, resource: Int, objects: Array<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view as TextView

        if (position == 0) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.gray))
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        textView.setPadding(15)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view as TextView
        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        textView.setPadding(15)
        return view
    }
}