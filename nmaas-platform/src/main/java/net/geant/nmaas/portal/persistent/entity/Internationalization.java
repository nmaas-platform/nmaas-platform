package net.geant.nmaas.portal.persistent.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
//@AllArgsConstructor
@Getter
@Setter
//@Builder
@Entity
public class Internationalization extends InternationalizationAbstract {

    @Lob
    @Type(type= "text")
    @Column
    private String content;

    //since lombok does not provide all args constructor when extending super class
    public Internationalization(long id, String language, boolean enabled, String content){
        super(id, language, enabled);
        this.content = content;
    }

    /**
     * converts Internalization object to InternationalizationSimpleObject
     * @return InternationalizationSimple object with content serialized into InternationalizationNodes
     */
    public InternationalizationSimple getAsInternationalizationSimple(){
        InternationalizationSimple result = new InternationalizationSimple();
        // copy trivial properties
        result.setId(this.getId());
        result.setEnabled(this.isEnabled());
        result.setLanguage(this.getLanguage());
        // deserialize content
        // composite-like solution
        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode node = om.readTree(this.getContent());
            List<InternationalizationNode> parts = new ArrayList<>();
            decompose(parts, node, "");
            result.setLanguageNodes(parts);
            return result;
        } catch (IOException ioe){
            throw new IllegalArgumentException("Language content cannot be parsed");
        }
    }

    /**
     * recursively traverses JsonNode object, retrieving keys and values, appending them to result list
     * @param result output list
     * @param node root node
     * @param key current key value
     */
    private void decompose(List<InternationalizationNode> result, JsonNode node, String key){
        if(!node.isContainerNode()){
            if(node.isTextual()){
                result.add(new InternationalizationNode(key, node.textValue()));
            } else {
                throw new IllegalArgumentException("Language node is not textual");
            }
        } else {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> e = it.next();

                String outKey = key.equals("") ? e.getKey(): key+"."+e.getKey();

                decompose(result, e.getValue(), outKey);
            }
        }

    }
}