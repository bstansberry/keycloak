package org.keycloak.models.mongo.impl.context;

import org.keycloak.models.mongo.api.MongoEntity;
import org.keycloak.models.mongo.api.MongoIdentifiableEntity;
import org.keycloak.models.mongo.api.MongoStore;
import org.keycloak.models.mongo.api.context.MongoStoreInvocationContext;
import org.keycloak.models.mongo.api.context.MongoTask;

/**
 * Context, which is not doing any postponing of tasks and does not cache anything
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SimpleMongoStoreInvocationContext implements MongoStoreInvocationContext {

    private final MongoStore mongoStore;

    public SimpleMongoStoreInvocationContext(MongoStore mongoStore) {
        this.mongoStore = mongoStore;
    }

    @Override
    public void addCreatedObject(MongoIdentifiableEntity entity) {
    }

    @Override
    public void addLoadedObject(MongoIdentifiableEntity entity) {
    }

    @Override
    public <T extends MongoIdentifiableEntity> T getLoadedObject(Class<T> type, String id) {
        return null;
    }

    @Override
    public void addUpdateTask(MongoIdentifiableEntity entityToUpdate, MongoTask task) {
        task.execute();
    }

    @Override
    public void addRemovedObject(MongoIdentifiableEntity entityToRemove) {
        entityToRemove.afterRemove(this);
    }

    @Override
    public void beforeDBSearch(Class<? extends MongoIdentifiableEntity> entityType) {
    }

    @Override
    public void begin() {
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    @Override
    public MongoStore getMongoStore() {
        return mongoStore;
    }
}
