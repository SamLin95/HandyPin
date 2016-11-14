package layout;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.slin77gatech.handypin.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VoteFragment.VoteInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VoteFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public static String TAG = "vote_fragment";
    private static final String VOTE_NUM = "vote_number";

    // TODO: Rename and change types of parameters
    private int voteNum;

    private Button mUpVoteBtn;
    private Button mDownVoteBtn;
    private TextView mVoteNum;


    private VoteInteractionListener mListener;

    public VoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment VoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VoteFragment newInstance(int param1) {
        VoteFragment fragment = new VoteFragment();
        Bundle args = new Bundle();
        args.putInt(VOTE_NUM, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            voteNum = getArguments().getInt(VOTE_NUM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_vote, container, false);
        mUpVoteBtn = (Button) v.findViewById(R.id.up_vote_btn);
        mDownVoteBtn = (Button)v.findViewById(R.id.down_vote_btn);
        mVoteNum = (TextView) v.findViewById(R.id.vote_num_text);

        mVoteNum.setText(String.valueOf(voteNum));

        mUpVoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.upvote();
                closeFragment();
            }
        });

        mDownVoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.downvote();
                closeFragment();
            }
        });

        return v;
    }

    private void closeFragment() {
        getActivity().getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(VoteFragment.this)
                .commit();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VoteInteractionListener) {
            mListener = (VoteInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface VoteInteractionListener {
        void upvote();
        void downvote();
    }
}
