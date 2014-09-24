package com.housetrip.pockettrailer;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "com.housetrip.pockettrailer");

        Entity pullRequest = schema.addEntity("PullRequest");
        pullRequest.addIdProperty();
        pullRequest.addStringProperty("title");
        pullRequest.addStringProperty("author");
        pullRequest.addStringProperty("state");
        pullRequest.addStringProperty("url");
        pullRequest.addIntProperty("number");
        pullRequest.addDateProperty("createdAt");

        pullRequest.setHasKeepSections(true);

        Entity repository = schema.addEntity("Repository");
        repository.addIdProperty();
        repository.addStringProperty("fullName");

        Property repositoryId = pullRequest.addLongProperty("repositoryId").notNull().getProperty();

        ToMany repositoryToPullRequests = repository.addToMany(pullRequest, repositoryId);
        pullRequest.addToOne(repository, repositoryId);

        new DaoGenerator().generateAll(schema, args[0]);
    }
}

