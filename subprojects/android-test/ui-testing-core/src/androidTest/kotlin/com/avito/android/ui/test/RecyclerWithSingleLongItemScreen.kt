package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.ListElement
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.ui.test.R

class RecyclerWithSingleLongItemScreen : PageObject() {
    val list: ListElement = element(withId(R.id.recycler))
}