package layout;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import edu.slin77gatech.handypin.R;
import edu.slin77gatech.handypin.utils.CommentsAdapter;
import edu.slin77gatech.handypin.utils.PinComment;


public class CommentsFragment extends Fragment {

    public static final String TAG = "comments_fragment";

    /**
     * Define interface for activity that will use this fragment.
     */
    public interface CommentsFragmentInteractionListerner {
        void sendNewComment(String comment);
    }

    // TODO: Rename parameter arguments, choose names that match

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String COMMNETS = "param1";
    private static final String AUTHORS = "param2";
    private static final String TIMESTAMPS = "params3";

    private String[] authors;
    private String[] comments;
    private String[] timestamps;

    private CommentsFragmentInteractionListerner mListener;

    Button mCloseBtn;

    TextView mNewCommentText;
    Button mSendNewCommentBtn;

    RecyclerView mCommentList;
    RecyclerView.LayoutManager mLayoutManager;


    public CommentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    //
    public static CommentsFragment newInstance(String[] authors, String[] comments, String[] timestamps) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putStringArray(AUTHORS, authors);
        args.putStringArray(COMMNETS, comments);
        args.putStringArray(TIMESTAMPS, timestamps);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            authors = getArguments().getStringArray(AUTHORS);
            comments = getArguments().getStringArray(COMMNETS);
            timestamps = getArguments().getStringArray(TIMESTAMPS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comments_fragment, container, false);
        setUpComments(v);
        mCloseBtn = (Button) v.findViewById(R.id.close_comments_fragment_btn);
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });

        mNewCommentText = (TextView)v.findViewById(R.id.new_comment_text);
        mSendNewCommentBtn = (Button) v.findViewById(R.id.send_my_comment_btn);

        mSendNewCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.sendNewComment(mNewCommentText.getText().toString());
                closeFragment();
            }
        });
        return v;
    }


    @Override
    public void onAttach(Context context) {
          super.onAttach(context);
        if (context instanceof CommentsFragmentInteractionListerner) {
            mListener = (CommentsFragmentInteractionListerner) context;
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
     * setUp comment list.
     */
    private void setUpComments(View fragmentRootView) {
        mCommentList = (RecyclerView) fragmentRootView.findViewById(R.id.comment_list);
        PinComment[] data = new PinComment[comments.length];
        PinComment newData;

        for (int i = 0; i < comments.length; i++) {
            newData = new PinComment(comments[i], authors[i], timestamps[i]);
            data[i] = newData;
        }

        CommentsAdapter adapter = new CommentsAdapter(data);
        mLayoutManager = new LinearLayoutManager(getContext());
        mCommentList.setAdapter(adapter);
        mCommentList.setLayoutManager(mLayoutManager);
    }

    private void closeFragment() {
        getActivity().getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(CommentsFragment.this)
                .commit();
    }
}
