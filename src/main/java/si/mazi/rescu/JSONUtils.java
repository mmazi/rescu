/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 * Copyright (C) 2012 - 2013 Matija Mazi matija.mazi@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package si.mazi.rescu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.utils.AssertUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Central place for common JSON operations
 */
public class JSONUtils {

    private static final Logger log = LoggerFactory.getLogger(JSONUtils.class);

    /**
     * private Constructor
     */
    private JSONUtils() {

    }

    /**
     * Creates a POJO from a Jackson-annotated class given a jsonString
     *
     * @param jsonString
     * @param returnType
     * @param objectMapper
     * @return
     */
    public static <T> T getJsonObject(String jsonString, Class<T> returnType, ObjectMapper objectMapper) {

        AssertUtil.notNull(jsonString, "jsonString cannot be null");
        if (jsonString.trim().length() == 0) {
            return null;
        }
        AssertUtil.notNull(objectMapper, "objectMapper cannot be null");
        try {
            return objectMapper.readValue(jsonString, returnType);
        } catch (IOException e) {
            log.error("Error unmarshalling from json: " + jsonString);
            throw new RestJsonException("Problem getting JSON object", e);
        }
    }

    /**
     * Get a generic map holding the raw data from the JSON string to allow manual type differentiation
     *
     * @param jsonString   The JSON string
     * @param objectMapper The object mapper
     * @return The map
     */
    public static Map<String, Object> getJsonGenericMap(String jsonString, ObjectMapper objectMapper) {

        AssertUtil.notNull(jsonString, "jsonString cannot be null");
        AssertUtil.notNull(objectMapper, "objectMapper cannot be null");
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RestJsonException("Problem getting JSON generic map", e);
        }
    }

    /**
     * Given any object return the JSON String representation of it
     *
     * @param object
     * @param objectMapper
     * @return
     */
    public static String getJSONString(Object object, ObjectMapper objectMapper) throws JsonProcessingException {

        AssertUtil.notNull(object, "object cannot be null");
        AssertUtil.notNull(objectMapper, "objectMapper cannot be null");
        return objectMapper.writeValueAsString(object);
    }

}
