package elfak.mosis.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import elfak.mosis.project.data.SpotsData
import elfak.mosis.project.databinding.FragmentListBinding
import elfak.mosis.project.model.SpotsViewModel


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val spotsViewModel: SpotsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lvSpotList.adapter = ArrayAdapter<SpotsData>(view.context, android.R.layout.simple_list_item_1, spotsViewModel.spots)
        binding.lvSpotList.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            spotsViewModel.selectSpot(spotsViewModel.spots[position].spotId)
            findNavController().navigate(R.id.action_ListFragment_to_ShowSpotFragment)
        })


    }

}