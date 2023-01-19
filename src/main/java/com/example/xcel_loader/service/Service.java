package ug.go.ursb.namesearch.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ug.go.ursb.namesearch.api.requests.AnalysisReportRequest;
import ug.go.ursb.namesearch.api.requests.BrsNamesUploadRequest;
import ug.go.ursb.namesearch.api.requests.NameSearchRequest;
import ug.go.ursb.namesearch.api.responses.*;
import ug.go.ursb.namesearch.exceptions.NameSearchException;
import ug.go.ursb.namesearch.helpers.Utils;
import ug.go.ursb.namesearch.models.ElasticSearchName;
import ug.go.ursb.namesearch.models.NameSearch;
import ug.go.ursb.namesearch.models.NameSearchSpecifications;
import ug.go.ursb.namesearch.models.RejectedName;
import ug.go.ursb.namesearch.store.NameSearchStore;
import ug.go.ursb.namesearch.store.RejectedNameStore;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ug.go.ursb.namesearch.helpers.CompanyNameTypes.typesFilter;
import static ug.go.ursb.namesearch.helpers.Constants.*;

@Service
public class NameSearchService {

    @Autowired
    private NameSearchStore nameSearchStore;

    @Autowired
    private RejectedNameStore rejectedNameStore;

    private final Pattern special = Pattern.compile("[!@#$%*_+=|<>?{}\\[\\]~]");


    public CustomApiResponse loadBrsNameFromExcel(BrsNamesUploadRequest req) {
        List<UUID> newPrn = new ArrayList<>();
        if (req.getFile().getOriginalFilename().endsWith(".xlsx")) {

            try {
                XSSFWorkbook workbook = new XSSFWorkbook(req.getFile().getInputStream());

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    var sheetAt = workbook.getSheetAt(i);
                    if (sheetAt.getSheetName().equals("names")) {

                        newPrn.addAll(loadUsedPrn(sheetAt));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            var brsNames = newPrn.stream().map(it -> NameSearchWrapper.nameSearchWrapper(nameSearchStore.findById(it))).collect(Collectors.toList());

            var customApiResponse = new CustomApiResponse();
            customApiResponse.setMessage("Name added to search");
            customApiResponse.setData(brsNames);
            customApiResponse.setStatus(HttpStatus.ACCEPTED.value());
            return customApiResponse;
        } else {
            var customApiResponse = new CustomApiResponse();
            customApiResponse.setMessage("File " + req.getFile().getContentType() + "not allowed");
            customApiResponse.setData(null);
            customApiResponse.setStatus(HttpStatus.ACCEPTED.value());
            return customApiResponse;
        }

    }


    private List<UUID> loadUsedPrn(XSSFSheet sheet) {

        Iterator<Row> rows = sheet.iterator();

        List<NameSearch> prnCreditedList = new ArrayList<>();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();

            // skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            Iterator<Cell> cellsInRow = currentRow.iterator();

            NameSearch name = new NameSearch();

            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();

                DataFormatter formatter = new DataFormatter();
                String value = formatter.formatCellValue(currentCell);
                System.out.println(value);

                switch (cellIdx) {
                    case 1:
                        name.setName(value);
                        break;
                    case 2:
                        if (!String.class.equals(value.getClass())) {
                            Date date = currentCell.getDateCellValue();
                            name.setCreatedAt(date);
                            break;
                        }
                    case 3:
                        if (!String.class.equals(value.getClass())) {
                            Date date = currentCell.getDateCellValue();
                            name.setCreatedAt(date);
                            break;
                        }
                    case 4:
                        name.setType(value);
                        break;
                    case 5:
                        name.setSubType(value);
                        break;
                    case 6:
                        name.setNo(value);
                        break;
                    case 7:
                        name.setStatus(value);
                        break;

                    default:
                        break;
                }

                cellIdx++;
            }
            prnCreditedList.add(name);
        }

        return prnCreditedList.stream().map(it -> nameSearchStore.saveNewName(it)).collect(Collectors.toList());
    }
}
