package com.example.xcel_loader.service;

import com.example.xcel_loader.controller.BrsNamesUploadRequest;
import com.example.xcel_loader.controller.CustomApiResponse;
import com.example.xcel_loader.controller.NameSearchWrapper;
import com.example.xcel_loader.model.BrnLists;
import com.example.xcel_loader.model.NameSearch;
import com.example.xcel_loader.store.Store;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.xcel_loader.model.Constants.*;


@Service
public class NameSearchService {

    @Autowired
    private Store nameSearchStore;


    private final Pattern special = Pattern.compile("[!@#$%*_+=|<>?{}\\[\\]~]");


    public CustomApiResponse loadBrsNameFromExcel(BrsNamesUploadRequest req) {
        List<UUID> newPrn = new ArrayList<>();
        List<NameSearch> duplicates = new ArrayList<>();

        if (req.getFile().getOriginalFilename().endsWith(".xlsx")) {

            try {
                XSSFWorkbook workbook = new XSSFWorkbook(req.getFile().getInputStream());

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    var sheetAt = workbook.getSheetAt(i);
                    if (sheetAt.getSheetName().equals("names")) {

                        var brnLists = loadUsedPrn(sheetAt);
                        if (!brnLists.getDuplicateNames().isEmpty()) {
                            duplicates.addAll(brnLists.getDuplicateNames());
                        } else {
                            newPrn.addAll(brnLists.getNewNames());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            var brsNames = newPrn.stream().map(it -> NameSearchWrapper.nameSearchWrapper(nameSearchStore.findById(it))).collect(Collectors.toList());
            if (duplicates.size() != 0) {
                var duplicateName = duplicates.stream().map(NameSearchWrapper::nameSearchWrapper).toList();
                var customApiResponse = new CustomApiResponse();
                customApiResponse.setMessage("These names were not added because they already exist");
                customApiResponse.setData(duplicateName);
                customApiResponse.setStatus(HttpStatus.CONFLICT.value());
                return customApiResponse;
            } else {
                var customApiResponse = new CustomApiResponse();
                customApiResponse.setMessage("Name added to search");
                customApiResponse.setData(brsNames);
                customApiResponse.setStatus(HttpStatus.OK.value());
                return customApiResponse;
            }
        } else {
            var customApiResponse = new CustomApiResponse();
            customApiResponse.setMessage("File " + req.getFile().getContentType() + "not allowed");
            customApiResponse.setData(null);
            customApiResponse.setStatus(HttpStatus.OK.value());
            return customApiResponse;
        }

    }


    private BrnLists loadUsedPrn(XSSFSheet sheet) {

        Iterator<Row> rows = sheet.iterator();

        List<NameSearch> oldBrns = new ArrayList<>();
        List<NameSearch> dulpicateBrns = new ArrayList<>();

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
                        if (!(value instanceof String)) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                            String dateString = currentCell.getDateCellValue().toString();
                            Date date;
                            try {
                                date = dateFormat.parse("01/20/2023");
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
//                            Date date = currentCell.getDateCellValue();
                            name.setCreatedAt(date);
                            break;
                        }
                    case 3:
                        if (!(value instanceof String)) {
                            Date date_ = new Date();
                            name.setUpdatedAt(date_);
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
            if (!nameSearchStore.existsByName(name.getName())) {
                oldBrns.add(name);
            } else {
                dulpicateBrns.add(name);
            }
        }

        var uuids = oldBrns.stream().map(it -> nameSearchStore.saveNewName(it)).collect(Collectors.toList());
        return BrnLists.builder().newNames(uuids).duplicateNames(dulpicateBrns).build();
    }


    @Transactional
    public void createName(NameSearch nameSearch) {
        var checkName = nameSearchStore.findByNoOrId(nameSearch.getNo(), nameSearch.getId());

        if (checkName.isPresent()) {

            var id = checkName.get().getId();

            if (nameSearch.getStatus().equalsIgnoreCase(STATUS_EXPIRED)) {

                nameSearchStore.removeName(id);

                return;
            }

            nameSearch.setId(id);
        }

        nameSearchStore.saveNewName(nameSearch);

        if (checkName.isPresent()) {

            var oldName = checkName.get();

            if (!oldName.getNo().equals(nameSearch.getNo()) && nameSearch.getStatus().equals(STATUS_REGISTERED)) {

                nameSearchStore.updateToRegistered(oldName.getNo(), nameSearch.getNo());

            } else if (!oldName.getNo().equals(nameSearch.getNo()) && nameSearch.getStatus().equals(STATUS_RESERVED)) {

                nameSearchStore.updateStatus(oldName.getNo(), STATUS_RESERVED);

            } else {

                if (!nameSearch.getStatus().equalsIgnoreCase(oldName.getStatus())) {
                    nameSearchStore.updateStatus(oldName.getNo(), nameSearch.getStatus());
                }

            }

        }

        System.out.println("New name line: " + nameSearch.getName() + " " + nameSearch.getNo() + " " + nameSearch.getStatus());
    }

    public void exportExcel(String filePath, List<List<Object>> data) {
        try {

            XSSFWorkbook workbook = new XSSFWorkbook();

            XSSFSheet sheet = workbook.createSheet("Data");

            for (int i = 0; i < data.size(); i++) {
                XSSFRow row = sheet.createRow(i);
                List<Object> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    XSSFCell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j).toString());
                }
            }

            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
