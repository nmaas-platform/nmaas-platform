package net.geant.nmaas.portal.persistence.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistence.entity.FileInfo;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
	List<FileInfo> getByContentType(String contentType);
}
