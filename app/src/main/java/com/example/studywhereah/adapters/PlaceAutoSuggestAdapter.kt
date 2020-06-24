package com.example.studywhereah.adapters

//import android.content.Context
//import android.widget.ArrayAdapter
//import android.widget.Filter
//import android.widget.Filterable
//
//class PlaceAutoSuggestAdapter : ArrayAdapter<String>(context, 0), Filterable {
//
//    lateinit var results: ArrayList<String>
//    lateinit var resource : Integer
//
//    override fun getFilter(): Filter? {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence): FilterResults {
//                val filterResults = FilterResults()
//                if (constraint != null) {
//                    //this will be an arraylist of the suggestions
//                    results = placeApi.autoComplete(constraint.toString())
//                    filterResults.values = results
//                    filterResults.count = results.size
//                }
//                return filterResults
//            }
//
//            override fun publishResults(constraint: CharSequence, results: FilterResults) {
//                if (results != null && results.count > 0) {
//                    notifyDataSetChanged()
//                } else {
//                    notifyDataSetInvalidated()
//                }
//            }
//        }
//    }
//
//}