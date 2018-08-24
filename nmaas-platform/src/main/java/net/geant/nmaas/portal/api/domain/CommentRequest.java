package net.geant.nmaas.portal.api.domain;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequest {
	Long parentId;

	String comment;
	
}
