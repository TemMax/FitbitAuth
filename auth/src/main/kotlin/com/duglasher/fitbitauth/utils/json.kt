package com.duglasher.fitbitauth.utils

import org.json.JSONArray
import org.json.JSONObject

internal inline fun <R, C : MutableCollection<R>> JSONArray.mapStringsTo(dest: C, transform: (String) -> R): C {
    repeat(length()) {
        dest.add(transform(getString(it)))
    }
    return dest
}

internal inline fun <R, C : MutableCollection<R>> JSONArray.mapObjsTo(dest: C, transform: (JSONObject) -> R): C {
    repeat(length()) {
        dest.add(transform(getJSONObject(it)))
    }
    return dest
}
