package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gte;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY;
import java.nio.ByteBuffer;
import java.util.List;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByEndpointGroupId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CassandraEPByEndpointGroupIdDao extends AbstractCassandraDao<CassandraEPByEndpointGroupId, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraEPByEndpointGroupIdDao.class);

    @Override
    protected Class<?> getColumnFamilyClass() {
        return CassandraEPByEndpointGroupId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_BY_ENDPOINT_GROUP_ID_COLUMN_FAMILY_NAME;
    }

    public ByteBuffer[] findFirstPageEPByEndpointGroupId(String endpointGroupId, String limit) {
        LOG.debug("Try to find first page of endpoint key hash by group id {} with limit {}", endpointGroupId, limit);
        List<CassandraEPByEndpointGroupId> filter = findListByStatement(select().from(getColumnFamilyName())
                .where(eq(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY, endpointGroupId))
                .limit(Integer.valueOf(limit) + 1));
        ByteBuffer[] endpointKeyHash = new ByteBuffer[filter.size()];
        int i = 0;
        for (CassandraEPByEndpointGroupId ep : filter) {
            endpointKeyHash[i++] = ep.getEndpointKeyHash();
        }
        return endpointKeyHash;
    }

    public ByteBuffer[] findEPByEndpointGroupId(String endpointGroupId, String limit, byte[] endpointKey) {
        LOG.debug("Try to find endpoint key hash list by endpoint group id {} with limit {} start from keyHash {}",
                endpointGroupId, limit, endpointKey);
        List<CassandraEPByEndpointGroupId> filter = findListByStatement(select().from(getColumnFamilyName())
                .where(eq(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_GROUP_ID_PROPERTY, endpointGroupId))
                .and(gte(EP_BY_ENDPOINT_GROUP_ID_ENDPOINT_KEY_HASH_PROPERTY, getByteBuffer(endpointKey)))
                .limit(Integer.valueOf(limit) + 1));
        ByteBuffer[] endpointKeyHash = new ByteBuffer[filter.size()];
        int i = 0;
        for (CassandraEPByEndpointGroupId ep : filter) {
            endpointKeyHash[i++] = ep.getEndpointKeyHash();
        }
        return endpointKeyHash;
    }
}
