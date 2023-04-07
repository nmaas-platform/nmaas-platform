package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.bulk.CsvReplay;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.CsvDeserializer;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvDeserializerImpl implements CsvDeserializer {

    public static String TYPE_CSV = "text/csv";

    private final UserService userService;

    private final DomainService domainService;



    /**
     * Read CSV file and map it to given class type
     * @param file an MultipartFile CSV from controller
     * @param givenClass an CSVClass created for reader of CSV file ( used to map fields)
     * @throws IOException thrown when provided file is invalid
     */

    public List<CsvReplay> deserializeCSV(MultipartFile file, Class givenClass) throws IOException {

//        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
        //TODO add dynamic path
        File fileLocal = new File("src/tmp.csv");
        try(OutputStream os = new FileOutputStream(fileLocal)) {
            os.write(file.getBytes());
        }

        try {
            List<CsvBean> resultInClass = beanBuilderExample(fileLocal.toPath(), givenClass);
            resultInClass.forEach(x -> System.out.println(x.toString()));

            if (givenClass.equals(CsvDomain.class)) {
               return this.createDomainAndUser(resultInClass);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<CsvBean> beanBuilderExample(Path path, Class clazz) throws Exception {

        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean cb = new CsvToBeanBuilder<CsvBean>(reader)
                    .withType(clazz)
                    .build();

            return cb.parse();
        }
    }

    private List<CsvReplay> createDomainAndUser(List<CsvBean> source) {
        log.error("in user creation");

        List<CsvReplay> result = new ArrayList<>();

        List<CsvDomain> list = source.stream().map(e -> (CsvDomain) e).collect(Collectors.toList());

        list.forEach( usr -> {
            Domain domain = null;
            if(!this.domainService.existsDomain(usr.getDomainName())) {
                domain = this.domainService.createDomain(new DomainRequest(usr.getDomainName(), usr.getDomainName(), true));
                Map<String, String> details = new HashMap<>();
                details.put("domain", domain.getId().toString());
                details.put("domainName", domain.getName());
                result.add(new CsvReplay(true, String.format("Domain %s created", domain.getName()), details, BulkType.DOMAIN));
            } else {
                domain = domainService.findDomain(usr.getDomainName()).get();
                Map<String, String> details = new HashMap<>();
                details.put("domain", domain.getId().toString());
                details.put("domainName", domain.getName());
                result.add(new CsvReplay(false, String.format("Domain %s already existed", domain.getName()), details, BulkType.DOMAIN));
            }
            // if user exist update role in domain to domain admin
            if(this.userService.existsByUsername(usr.getAdminUserName()) || this.userService.existsByEmail(usr.getEmail())) {
                log.error("user already created in database ", usr.getEmail());
                User user = this.userService.findByUsername(usr.getAdminUserName()).orElseGet(() -> this.userService.findByEmail(usr.getEmail()));
                if(!this.userService.hasPrivilege(user, domain, ROLE_DOMAIN_ADMIN)) {
                    user.setNewRoles(ImmutableSet.of(new UserRole(user, domain, ROLE_DOMAIN_ADMIN)));
                    this.userService.update(user);
                }
                Map<String, String> details = new HashMap<>();
                details.put("userId", user.getId().toString());
                details.put("userName", user.getUsername());
                details.put("email", user.getEmail());
                result.add(new CsvReplay(false, String.format("User %s already existed", user.getUsername()), details, BulkType.USER));
            } else {//if not create user
                User user = this.userService.registerBulk(usr, this.domainService.getGlobalDomain().get(), domain);
                Map<String, String> details = new HashMap<>();
                details.put("userId", user.getId().toString());
                details.put("userName", user.getUsername());
                details.put("email", user.getEmail());
                result.add(new CsvReplay(true, String.format("User %s created", user.getUsername()), details, BulkType.USER));
            }
        });
        return result;
    }

    public boolean isCSVFormat(MultipartFile file) {
        return TYPE_CSV.equals(file.getContentType());
    }
}
