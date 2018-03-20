package org.jetbrains.kotlinconf.data

class SessionizeData(val allData: AllData, val etag: String = allData.hashCode().toString())
