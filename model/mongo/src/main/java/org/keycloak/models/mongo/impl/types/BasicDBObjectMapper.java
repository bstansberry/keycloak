package org.keycloak.models.mongo.impl.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import org.jboss.logging.Logger;
import org.keycloak.models.mongo.api.MongoEntity;
import org.keycloak.models.mongo.api.MongoIdentifiableEntity;
import org.keycloak.models.mongo.api.types.Mapper;
import org.keycloak.models.mongo.api.types.MapperContext;
import org.keycloak.models.mongo.api.types.MapperRegistry;
import org.keycloak.models.mongo.impl.MongoStoreImpl;
import org.keycloak.models.mongo.impl.EntityInfo;
import org.picketlink.common.properties.Property;
import org.picketlink.common.reflection.Types;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BasicDBObjectMapper<S extends MongoEntity> implements Mapper<BasicDBObject, S> {

    private static final Logger logger = Logger.getLogger(BasicDBObjectMapper.class);

    private final MongoStoreImpl mongoStoreImpl;
    private final MapperRegistry mapperRegistry;
    private final Class<S> expectedObjectType;

    public BasicDBObjectMapper(MongoStoreImpl mongoStoreImpl, MapperRegistry mapperRegistry, Class<S> expectedObjectType) {
        this.mongoStoreImpl = mongoStoreImpl;
        this.mapperRegistry = mapperRegistry;
        this.expectedObjectType = expectedObjectType;
    }

    @Override
    public S convertObject(MapperContext<BasicDBObject, S> context) {
        BasicDBObject dbObject = context.getObjectToConvert();
        if (dbObject == null) {
            return null;
        }

        EntityInfo entityInfo = mongoStoreImpl.getEntityInfo(expectedObjectType);

        S object;
        try {
            object = expectedObjectType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (String key : dbObject.keySet()) {
            Object value = dbObject.get(key);
            Property<Object> property;

            if ("_id".equals(key)) {
                // Current property is "id"
                if (object instanceof MongoIdentifiableEntity) {
                    ((MongoIdentifiableEntity)object).setId(value.toString());
                }

            } else if ((property = entityInfo.getPropertyByName(key)) != null) {
                // It's declared property with @DBField annotation
                setPropertyValue(object, value, property);

            } else {
                // Show warning if it's unknown
                logger.warn("Property with key " + key + " not known for type " + expectedObjectType);
            }
        }

        return object;
    }

    private void setPropertyValue(MongoEntity object, Object valueFromDB, Property property) {
        if (valueFromDB == null) {
            property.setValue(object, null);
            return;
        }

        MapperContext<Object, ?> context;

        Type type = property.getBaseType();

        // This can be the case when we have parameterized type (like "List<String>")
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            Type[] genericTypeArguments = parameterized.getActualTypeArguments();

            List<Class<?>> genericTypes = new ArrayList<Class<?>>();
            for (Type genericType : genericTypeArguments) {
                genericTypes.add((Class<?>)genericType);
            }

            Class<?> expectedReturnType = (Class<?>)parameterized.getRawType();
            context = new MapperContext<Object, Object>(valueFromDB, expectedReturnType, genericTypes);
        } else {
            Class<?> expectedReturnType = (Class<?>)type;
            // handle primitives
            expectedReturnType = Types.boxedClass(expectedReturnType);
            context = new MapperContext<Object, Object>(valueFromDB, expectedReturnType, null);
        }

        Object appObject = mapperRegistry.convertDBObjectToApplicationObject(context);

        if (Types.boxedClass(property.getJavaClass()).isAssignableFrom(appObject.getClass())) {
            property.setValue(object, appObject);
        } else {
            throw new IllegalStateException("Converted object " + appObject + " is not of type " +  context.getExpectedReturnType() +
                    ". So can't be assigned as property " + property.getName() + " of " + object.getClass());
        }
    }

    @Override
    public Class<? extends BasicDBObject> getTypeOfObjectToConvert() {
        return BasicDBObject.class;
    }

    @Override
    public Class<S> getExpectedReturnType() {
        return expectedObjectType;
    }
}
