package kr.co.infomark.soundmasking.main

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import kr.co.infomark.soundmasking.MainActivity
import kr.co.infomark.soundmasking.R
import kr.co.infomark.soundmasking.databinding.FragmentListBinding
import kr.co.infomark.soundmasking.databinding.PlaylistItemBinding
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    val metaRetriever = MediaMetadataRetriever()
    var binding : FragmentListBinding? = null
    var playListAdapter : PlayListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_list,container,false)
        binding?.fileRecyclerview?.layoutManager = LinearLayoutManager(requireContext())
        binding?.fileRecyclerview?.apply {
            val mainActivity = activity as? MainActivity
            playListAdapter = PlayListAdapter(mainActivity?.musicBox?.playFiles)
            adapter = playListAdapter
        }
        var mainActivity = activity as? MainActivity
        mainActivity?.musicBox?.currentPlayMusicName?.observe(viewLifecycleOwner) {
            binding?.fileRecyclerview?.adapter?.notifyDataSetChanged()
        }

        binding?.swipeRefresh?.setOnRefreshListener(OnRefreshListener { // Your code to refresh the list here.
            reloadData()

        })

        return binding?.root
    }
    fun reloadData(){
        var mainActivity = activity as? MainActivity
        binding?.swipeRefresh?.isRefreshing = true
        mainActivity?.musicBox?.stopMusic()
        mainActivity?.loadFile()
        Handler(Looper.getMainLooper()).postDelayed({
            refreshList()
            mainActivity?.initSetting()
            binding?.swipeRefresh?.isRefreshing = false
        },1500)
    }

    override fun onResume() {
        super.onResume()
        binding?.swipeRefresh?.isRefreshing = false
        refreshList()

    }
    fun refreshList(){
        val mainActivity = activity as? MainActivity
        playListAdapter?.files = mainActivity?.musicBox?.playFiles
        playListAdapter?.notifyDataSetChanged()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    inner class PlayListAdapter(var files: MutableList<File>?) : RecyclerView.Adapter<PlayListAdapter.PlayListItemViewHolder>() {

        inner class PlayListItemViewHolder(val binding: PlaylistItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListItemViewHolder {
            val view = DataBindingUtil.inflate<PlaylistItemBinding>(LayoutInflater.from(parent.context),
                R.layout.playlist_item, parent, false)
            return PlayListItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlayListItemViewHolder, position: Int) {
            files?.get(position)?.let { file ->
                var mainActivity = activity as? MainActivity

                if(mainActivity?.musicBox?.currentPlayMusicName?.value == file.name && mainActivity?.musicBox?.selectFileSize == file.length() && mainActivity?.musicBox?.isPlay?.value == true){
                    holder.binding.arrowIcon.setImageResource(R.drawable.ico_playlist_pause)
                    holder.itemView.setOnClickListener {
                        var mainActivity = activity as? MainActivity
                        mainActivity?.musicBox?.stopMusic()
                        mainActivity?.musicBox?.currentPlayMusicName?.value = ""
//                        mainActivity?.musicBox?.currentIndex = 0
                        notifyDataSetChanged()
                    }
                }else{
                    holder.binding.arrowIcon.setImageResource(R.drawable.ico_playlist_play)
                    holder.itemView.setOnClickListener {
                        var mainActivity = activity as? MainActivity
                        mainActivity?.musicBox?.playMusic(file.path)
                        mainActivity?.musicBox?.currentIndex = position
                        notifyDataSetChanged()
                    }
                }

                holder.binding.contentTextview.text = file.name
                holder.binding.timelineTextview.text = getDurationWithMp3Spi(file.absolutePath)

            }


        }

        override fun getItemCount(): Int {
            return files?.size ?: 0
        }

        private fun getDurationWithMp3Spi(path: String) : String {
            try {
                metaRetriever.setDataSource(path)
            }catch (e : IllegalArgumentException){
                reloadData()
            }


            var out = ""

            val duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val dur = duration?.toLong() ?: 0L
            val seconds = (dur % 60000 / 1000).toString()

            val minutes = (dur / 60000).toString()

            if (seconds.length == 1) {

                    out =  "0$minutes:0$seconds"
            } else {
                out = "0$minutes:$seconds"

            }
            return out
        }

    }
}