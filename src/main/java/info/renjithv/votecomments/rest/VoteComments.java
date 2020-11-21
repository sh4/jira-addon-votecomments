package info.renjithv.votecomments.rest;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.transaction.TransactionCallback;
import info.renjithv.votecomments.VoteInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Hashtable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A resource of message.
 */
@Path("/")
@Singleton
@Component
public class VoteComments {

    private static final Logger log = LogManager.getLogger("votecomments");
    private final ActiveObjects ao;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final CommentManager commentManager;
    private final UserManager userManager;

    @Autowired
    public VoteComments(@ComponentImport final ActiveObjects ao,
                        @ComponentImport final IssueManager issueManager,
                        @ComponentImport final PermissionManager permissionManager,
                        @ComponentImport final CommentManager commentManager,
                        @ComponentImport final UserManager userManager)
    {
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.commentManager = commentManager;
        this.ao = checkNotNull(ao);
        this.userManager = userManager;
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("commentsvotes")
    public Response getIssueCommentsVotes(@QueryParam("issueid") final Long issueid) {
        if (null == issueid) {
            return Response.notModified("Issue Id missing").build();
        }
        else {
            log.debug("Fetching comment votes for issue: " + issueid);
        }

        final ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        final Hashtable<Long, VoteCommentsModel> data = new Hashtable<Long, VoteCommentsModel>();
        final MutableIssue issueObject = issueManager.getIssueObject(issueid);

        if (null != issueObject && permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issueObject, loggedInUser)) {
            ao.executeInTransaction(new TransactionCallback<Void>() {
                @Override
                public Void doInTransaction() {
                    VoteInfo[] votes = ao.find(VoteInfo.class, "ISSUE_ID = ?", issueid);
                    for (VoteInfo voteInfo : votes) {
                        log.info("Vote id - " + voteInfo.getID());
                        VoteCommentsModel inData = new VoteCommentsModel(voteInfo.getCommentId(), 0, 0);
                        if (data.containsKey(voteInfo.getCommentId())) {
                            inData = data.get(voteInfo.getCommentId());
                        }
                        VoterUserModel model = getVoterUserModel(voteInfo);
                        switch (voteInfo.getVoteCount()) {
                            case -1:
                                inData.setDownVotes(inData.getDownVotes() + 1, model);
                                break;
                            case 1:
                                inData.setUpVotes(inData.getUpVotes() + 1, model);
                                break;
                            default:
                                log.error("No way this can happen");
                        }
                        data.put(voteInfo.getCommentId(), inData);
                    }
                    return null;
                }
            });
        }
        else
        {
            log.warn("Get votes request ignored");
        }
        return Response.ok(data.values()).build();
    }

    @Nonnull
    private VoterUserModel getVoterUserModel(VoteInfo voteInfo) {
        ApplicationUser voter = userManager.getUserByName(voteInfo.getUserName());
        String userName = voteInfo.getUserName();
        String displayName = (voter != null)  ? voter.getDisplayName() : userName;
        VoterUserModel model = new VoterUserModel(userName, displayName);
        return model;
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("upvote")
    public Response upvoteComment(@QueryParam("commentid") final Long commentid, @QueryParam("issueid") final Long issueid) {
        if (null == issueid || null == commentid) {
            return Response.notModified("Required parameters missing").build();
        }
        UpdateVote(1, commentid, issueid);
        return Response.ok(new VoteCommentsModel(commentid, 0, 0)).build();
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("downvote")
    public Response downvoteComment(@QueryParam("commentid") Long commentid, @QueryParam("issueid") final Long issueid) {
        if (null == issueid || null == commentid) {
            return Response.notModified("Required parameters missing").build();
        }
        UpdateVote(-1, commentid, issueid);
        return Response.ok(new VoteCommentsModel(commentid, 0, 0)).build();
    }

    private void UpdateVote(final Integer increment, final Long commentid, final Long issueid) {
        final ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        final MutableIssue issueObject = issueManager.getIssueObject(issueid);
        final Comment comment = commentManager.getCommentById(commentid);

        if (null != issueObject &&
                null != loggedInUser &&
                permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issueObject, loggedInUser) &&
                null != comment &&
                !comment.getAuthorUser().equals(loggedInUser)) {

            ao.executeInTransaction(new TransactionCallback<Void>() {
                @Override
                public Void doInTransaction() {
                    VoteInfo[] votes = ao.find(VoteInfo.class, "COMMENT_ID = ? AND USER_NAME = ? AND ISSUE_ID = ?",
                            commentid, loggedInUser.getName(), issueid);
                    switch (votes.length) {
                        case 0:
                            final VoteInfo voteInfo = ao.create(VoteInfo.class);
                            voteInfo.setCommentId(commentid);
                            voteInfo.setIssueId(issueid);
                            voteInfo.setUserName(loggedInUser.getName());
                            voteInfo.setVoteCount(increment);
                            voteInfo.save();
                            break;
                        case 1:
                            log.info("Existing vote found for this user, comment and issue");
                            Integer vote = votes[0].getVoteCount();
                            vote = vote + increment;
                            /*
                             * -1 + 1  = 0 = delete
                             * 0  + 1  => This is not possible
                             * 1  + 1  => 2 = 1
                             * -1 + -1 => -2 = -1
                             * 0  + -1 => This is not possible
                             * 1  + -1 = 0 = delete
                             * */
                            switch (vote) {
                                case 0:
                                    ao.delete(votes[0]);
                                    break;
                                case 2:
                                    vote = 1;
                                    votes[0].setVoteCount(vote);
                                    votes[0].save();
                                    break;
                                case -2:
                                    vote = -1;
                                    votes[0].setVoteCount(vote);
                                    votes[0].save();
                                    break;
                                default:
                                    log.warn("This case should never come for vote count");
                                    break;
                            }
                            break;
                        default:
                            log.error("More that one vote found for the same comment from same user, this should never happen");
                    }
                    return null;
                }
            });
        } else {
            log.warn("Update vote request ignored");
        }
    }
}