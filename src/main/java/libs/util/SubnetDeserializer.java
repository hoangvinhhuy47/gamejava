package libs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;

/**
 * Created by tuanhoang on 4/13/17.
 */
public class SubnetDeserializer extends JsonDeserializer<SubnetUtils.SubnetInfo> {
    @Override
    public SubnetUtils.SubnetInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        SubnetUtils subnetUtils = new SubnetUtils(p.getValueAsString());
        subnetUtils.setInclusiveHostCount(true);
        return subnetUtils.getInfo();
    }
}
