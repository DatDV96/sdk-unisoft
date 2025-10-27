package com.ad.sample.onboard

interface PageListener {
    fun removePage(tag: String?)
    fun onNextPage()
    fun onPreviousPage()
}