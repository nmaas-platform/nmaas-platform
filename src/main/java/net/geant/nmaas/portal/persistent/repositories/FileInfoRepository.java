package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.FileInfo;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
	List<FileInfo> getByContentType(String contentType);
}
