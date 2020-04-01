package net.geant.nmaas.portal.persistent.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class InternationalizationSimple extends InternationalizationAbstract{

    @ElementCollection
    private List<InternationalizationNode> languageNodes;

    /**
     * converts InternationalizationSimple object to Internationalization
     * @return
     */
    public Internationalization getAsInternationalization(){
        Internationalization result =  new Internationalization(); //create simple object
        // rewrite trivial properties
        result.setId(this.getId());
        result.setLanguage(this.getLanguage());
        result.setEnabled(this.isEnabled());

        // serialize Internationalization nodes to single JSON content string
        Map<String, Object> contentStructure = new HashMap<>();
        for (InternationalizationNode in: this.getLanguageNodes()){
            Map<String, Object> currentNode = contentStructure;
            String[] keys = in.getKey().split("\\.");

            for(int i=0; i<keys.length; i++){
                boolean isLastKey = i == keys.length-1;
                if(isLastKey && !currentNode.containsKey(keys[i])){
                    // last key - put value under proper key
                    currentNode.put(keys[i], in.getContent());
                } else if (isLastKey) {
                    throw new IllegalArgumentException("Duplicated key");
                } else {
                    // else - change current node to sub-map, if not exists than create one
                    if(!currentNode.containsKey(keys[i])){
                        currentNode.put(keys[i], new HashMap<String, Object>());
                    }
                    currentNode = (Map<String, Object>)currentNode.get(keys[i]);
                }
            }
        }

        try{
            result.setContent(new ObjectMapper().writeValueAsString(contentStructure));
            return result;
        } catch (JsonProcessingException jpe){
            throw new IllegalArgumentException("Should not occur");
        }
    }

}
