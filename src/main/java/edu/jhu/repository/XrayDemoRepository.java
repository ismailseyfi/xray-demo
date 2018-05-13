package edu.jhu.repository;


import edu.jhu.controller.data.XrayDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository used to work with internal RDS storage.
 */
@Repository
public interface XrayDemoRepository extends CrudRepository<XrayDto, Long> {
    List<XrayDto> findByLastName(@Param("firstName") String firstName);
}
