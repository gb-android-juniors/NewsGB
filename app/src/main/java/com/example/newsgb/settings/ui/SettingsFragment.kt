package com.example.newsgb.settings.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.example.newsgb.R
import com.example.newsgb.databinding.SettingsFragmentBinding


class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences
    private val APP_PREFERENCES_COUNTRY = "country"
    private var countrySettings: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val items = listOf("Russia", "Deutch", "England")
        val adapter = ArrayAdapter(requireContext(), R.layout.country_list_item, items)
        (binding.selectCountryLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    override fun onPause() {
        super.onPause()
        val editor = prefs.edit()
        editor.putString(APP_PREFERENCES_COUNTRY, countrySettings).apply()
    }

    override fun onResume() {
        super.onResume()
        if(prefs.contains(APP_PREFERENCES_COUNTRY)){
            //TODO
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}