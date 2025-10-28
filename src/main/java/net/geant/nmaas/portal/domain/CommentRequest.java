package net.geant.nmaas.portal.domain;

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
