package it.alessandromencarini.droidtrailer;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "it.alessandromencarini.droidtrailer");

        schema.enableKeepSectionsByDefault();

        Entity pullRequest = schema.addEntity("PullRequest");
        pullRequest.addIdProperty();
        pullRequest.addStringProperty("title");
        pullRequest.addStringProperty("userLogin");
        pullRequest.addStringProperty("state");
        pullRequest.addStringProperty("url");
        pullRequest.addStringProperty("userAvatarUrl");
        pullRequest.addIntProperty("number");
        pullRequest.addIntProperty("commentCount");
        pullRequest.addIntProperty("unreadCommentCount");
        pullRequest.addBooleanProperty("assignedToMe");
        pullRequest.addBooleanProperty("mergeable");
        pullRequest.addDateProperty("createdAt");
        pullRequest.addDateProperty("closedAt");
        pullRequest.addDateProperty("mergedAt");
        pullRequest.addDateProperty("readAt");

        Entity repository = schema.addEntity("Repository");
        repository.addIdProperty();
        repository.addStringProperty("fullName");
        repository.addStringProperty("url");
        repository.addLongProperty("remoteId");
        repository.addBooleanProperty("selected");
        repository.addDateProperty("readAt");

        Entity comment = schema.addEntity("Comment");
        comment.addIdProperty();
        comment.addStringProperty("userAvatarUrl");
        comment.addStringProperty("body");
        comment.addStringProperty("userLogin");
        comment.addStringProperty("url");
        comment.addDateProperty("createdAt");

        Property repositoryId = pullRequest.addLongProperty("repositoryId").notNull().getProperty();
        repository.addToMany(pullRequest, repositoryId);
        pullRequest.addToOne(repository, repositoryId);

        Property pullRequestId = comment.addLongProperty("pullRequestId").notNull().getProperty();
        pullRequest.addToMany(comment, pullRequestId );
        comment.addToOne(pullRequest, pullRequestId );

        new DaoGenerator().generateAll(schema, args[0]);
    }
}
