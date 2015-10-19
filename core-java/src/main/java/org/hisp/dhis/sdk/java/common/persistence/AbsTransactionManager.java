package org.hisp.dhis.sdk.java.common.persistence;

import org.hisp.dhis.java.sdk.models.common.base.IdentifiableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hisp.dhis.java.sdk.models.common.base.BaseIdentifiableObject.toMap;

public abstract class AbsTransactionManager implements ITransactionManager {

    /**
     * {@inheritDoc}
     */
    public <T extends IdentifiableObject> List<IDbOperation> createOperations(IIdentifiableObjectStore<T> modelStore,
                                                                                     List<T> oldModels, List<T> newModels) {
        List<IDbOperation> ops = new ArrayList<>();

        Map<String, T> newModelsMap = toMap(newModels);
        Map<String, T> oldModelsMap = toMap(oldModels);

        // As we will go through map of persisted items, we will try to update existing data.
        // Also, during each iteration we will remove old model key from list of new models.
        // As the result, the list of remaining items in newModelsMap,
        // will contain only those items which were not inserted before.
        for (String oldModelKey : oldModelsMap.keySet()) {
            T newModel = newModelsMap.get(oldModelKey);
            T oldModel = oldModelsMap.get(oldModelKey);

            // if there is no particular model with given uid in list of
            // actual (up to date) items, it means it was removed on the server side
            if (newModel == null) {
                ops.add(DbOperation.with(modelStore)
                        .delete(oldModel));

                // in case if there is no new model object,
                // we can jump to next iteration.
                continue;
            }

            // if the last updated field in up to date model is after the same
            // field in persisted model, it means we need to update it.
            if (newModel.getLastUpdated().isAfter(oldModel.getLastUpdated())) {
                // note, we need to pass database primary id to updated model
                // in order to avoid creation of new object.
                newModel.setId(oldModel.getId());
                ops.add(DbOperation.with(modelStore)
                        .update(newModel));
            }

            // as we have processed given old (persisted) model,
            // we can remove it from map of new models.
            newModelsMap.remove(oldModelKey);
        }

        // Inserting new items.
        for (String newModelKey : newModelsMap.keySet()) {
            T item = newModelsMap.get(newModelKey);
            ops.add(DbOperation.with(modelStore)
                    .insert(item));
        }

        return ops;
    }
}
