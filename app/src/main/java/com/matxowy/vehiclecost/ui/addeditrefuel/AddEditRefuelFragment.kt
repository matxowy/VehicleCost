package com.matxowy.vehiclecost.ui.addeditrefuel

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.matxowy.vehiclecost.R

class AddEditRefuelFragment : Fragment() {

    companion object {
        fun newInstance() = AddEditRefuelFragment()
    }

    private lateinit var viewModel: AddEditRefuelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_edit_refuel_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddEditRefuelViewModel::class.java)
        // TODO: Use the ViewModel
    }

}