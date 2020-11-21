package info.renjithv.votecomments.rest;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "commentvotes")
@XmlAccessorType(XmlAccessType.FIELD)
public class VoteCommentsModel {

    @XmlElement(name = "commentid")
    private Long commentId;

    @XmlElement(name = "upvotes")
    private Integer upVotes;

    @XmlElement(name = "downvotes")
    private Integer downVotes;

    @XmlElement(name = "upvoters")
    private List<VoterUserModel> upVoteUsers;

    @XmlElement(name = "downvoters")
    private List<VoterUserModel> downVoteUsers;

    public VoteCommentsModel() {
    }

    public VoteCommentsModel(Long commentId, Integer upVotes, Integer downVotes) {
        this.commentId = commentId;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
        this.upVoteUsers = new ArrayList<>();
        this.downVoteUsers = new ArrayList<>();
    }

    public Integer getDownVotes() {
        return downVotes;
    }

    public void setDownVotes(Integer downVotes, VoterUserModel voter) {
        this.downVotes = downVotes;
        this.downVoteUsers.add(voter);
    }

    public Integer getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(Integer upVotes, VoterUserModel voter) {
        this.upVotes = upVotes;
        this.upVoteUsers.add(voter);
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
}