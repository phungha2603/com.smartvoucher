package com.smartvoucher.webEcommercesmartvoucher.service.impl;

import com.google.api.services.drive.model.File;
import com.smartvoucher.webEcommercesmartvoucher.converter.MerchantConverter;
import com.smartvoucher.webEcommercesmartvoucher.dto.MerchantDTO;
import com.smartvoucher.webEcommercesmartvoucher.entity.MerchantEntity;
import com.smartvoucher.webEcommercesmartvoucher.exception.DuplicationCodeException;
import com.smartvoucher.webEcommercesmartvoucher.exception.ObjectNotFoundException;
import com.smartvoucher.webEcommercesmartvoucher.repository.IMerchantRepository;
import com.smartvoucher.webEcommercesmartvoucher.service.IMerchantService;
import com.smartvoucher.webEcommercesmartvoucher.util.UploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class MerchantService implements IMerchantService {

    private final IMerchantRepository merchantRepository;
    private final MerchantConverter merchantConverter;
    private final UploadUtil uploadUtil;

    @Autowired
    public MerchantService(final  IMerchantRepository merchantRepository,
                           final MerchantConverter merchantConverter,
                           final UploadUtil uploadUtil){
        this.merchantRepository = merchantRepository;
        this.merchantConverter = merchantConverter;
        this.uploadUtil = uploadUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantDTO> getAllMerchant() {
        List<MerchantEntity> merchantEntityList = merchantRepository.findAllByStatus(1);
        if (merchantEntityList.isEmpty()){
            log.info("List merchant is empty !");
            throw new ObjectNotFoundException(
                    404, "List merchant is empty !"
            );
        }
        log.info("Get all merchant is completed !");
        return merchantConverter.toMerchantDTOList(merchantEntityList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantDTO> getAllMerchantCode(MerchantDTO merchantDTO) {
        List<MerchantEntity> merchantEntityList = merchantRepository.findAllByMerchantCode(
                merchantDTO.getMerchantCode().trim());
        return merchantConverter.toMerchantDTOList(merchantEntityList);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantDTO getMerchantCode(MerchantDTO merchantDTO) {
        MerchantEntity merchant = merchantRepository.findOneByMerchantCode(merchantDTO.getMerchantCode());
        return merchantConverter.toMerchantDTO(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDTO upsertMerchant(MerchantDTO merchantDTO) {
        MerchantEntity merchant;
        if (merchantDTO.getId() != null){
            boolean exist = existMerchant(merchantDTO);
            if (!exist){
                log.info("Cannot update merchant id: "+merchantDTO.getId());
                throw new ObjectNotFoundException(
                        404, "Cannot update merchant id: "+merchantDTO.getId()
                );
            }
            MerchantEntity oldMerchant = merchantRepository.findOneById(merchantDTO.getId());
            merchant = merchantConverter.toMerchantEntity(merchantDTO, oldMerchant);
            log.info("Update merchant is completed !");
        }else{
            List<MerchantEntity> merchantEntityList = merchantConverter.toMerchantEntityList(getAllMerchantCode(merchantDTO));
            if (!merchantEntityList.isEmpty()){
                log.info("Merchant code is duplicated !");
                throw new DuplicationCodeException(
                        400, "Merchant code is duplicated !"
                );
            }
            merchant = merchantConverter.toMerchantEntity(merchantDTO);
            log.info("Insert merchant is completed !");
        }
        return merchantConverter.toMerchantDTO(merchantRepository.save(merchant));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMerchant(MerchantDTO merchantDTO) {
        boolean exist = merchantRepository.existsById(merchantDTO.getId());
        if (!exist){
            log.info("Cannot delete id: "+merchantDTO.getId());
            throw new ObjectNotFoundException(
                    404, "Cannot delete id: "+merchantDTO.getId()
            );
        }
        log.info("Delete merchant is completed !");
        this.merchantRepository.deleteById(merchantDTO.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existMerchant(MerchantDTO merchantDTO) {
        return merchantRepository.existsById(merchantDTO.getId());
    }

    @Override
    public String uploadMerchantImages(MultipartFile fileName) {
        String folderId = "1z6B_EyGuGN5AJX8tqrGnZkT6XMiKuTg5";
        File file = uploadUtil.uploadImages(fileName, folderId);
        return "https://drive.google.com/uc?export=view&id="+file.getId();
    }
}
