package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentView {

	private Long id;
	private Long parentId;
	private UserBase owner;
	private Date createdAt;
	private String comment;
	private boolean deleted;
	
	List<CommentView> subComments = new ArrayList<>();
}
