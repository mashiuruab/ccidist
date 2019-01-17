--
-- This is only valid for oracle
--

insert into organization (id, name, created, updated, version) values ('polaris', 'Polaris', sysdate, sysdate, 0);
insert into organization (id, name, created, updated, version) values ('nhst', 'NHST', sysdate, sysdate, 0);
insert into organization (id, name, created, updated, version) values ('axelspringer', 'AxelSpringer', sysdate, sysdate, 0);
insert into organization (id, name, created, updated, version) values ('CCIEurope', 'CCIEurope', sysdate, sysdate, 0);

insert into publication (id, name, organization_id, created, updated, version) values ('addressa', 'Addressa', 'polaris', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('harstadtidende', 'Harstadtidende', 'polaris', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('nhst-sports', 'NHST-SPORTS', 'nhst', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('nhst-news', 'NHST-NEWS', 'nhst', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('axelSpringer-sports', 'AxelSpringer-SPORTS', 'axelspringer', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('axelSpringer-entertainment', 'AxelSpringer-ENTERTAINMENT', 'axelspringer', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('R2-Gazette', 'R2-Gazette', 'CCIEurope', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('Chronicle', 'Chronicle', 'CCIEurope', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('DigitalNGToday', 'DigitalNGToday', 'CCIEurope', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('DigiWorld', 'DigiWorld', 'CCIEurope', sysdate, sysdate, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('iPad-P', 'iPad-P', 'CCIEurope', sysdate, sysdate, 0);

insert into design_to_epub_mapper (id, design_name, epub_name, version) values (ccidist_seq_epub_mapper.nextval, 'ipad', 'ipad2', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (ccidist_seq_epub_mapper.nextval, 'ipad', 'ipad3', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (ccidist_seq_epub_mapper.nextval, 'ipad-mini', 'ipad-mini-1', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (ccidist_seq_epub_mapper.nextval, 'ipad', 'ipad-mini', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (ccidist_seq_epub_mapper.nextval, 'ipad', 'ipad4', 0);

insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'addressa', 1, 1, 'ios', '6.0', 0, '1.0', 'ipad2', sysdate , sysdate + 1, sysdate , sysdate);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'addressa', 2, 1, 'ios', '6.0', 0, '1.0', 'ipad3', sysdate , sysdate + 1, sysdate , sysdate);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'addressa', 3, 1, 'ios', '6.0', 0, '2.0', 'ipad-mini', sysdate , sysdate + 1, sysdate , sysdate);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'addressa', 4, 1, 'ios', '6.0', 0, '2.0', 'ipad-mini', sysdate , sysdate + 1, sysdate , sysdate);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'addressa', 5, 0, 'ios', '6.0', 0, '2.0', 'ipad4', sysdate , sysdate + 1, sysdate , sysdate);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'Chronicle'     , 1, 1, 'ios', '6.0', 0, '1.0', 'ipad', to_date('2011-01-01', 'YYYY-MM-DD'), to_date('2015-01-01', 'YYYY-MM-DD'), sysdate , sysdate); 
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'DigitalNGToday', 1, 1, 'ios', '6.0', 0, '1.0', 'ipad', to_date('2011-01-01', 'YYYY-MM-DD'), to_date('2015-01-01', 'YYYY-MM-DD'), sysdate , sysdate); 
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'iPad-P'        , 1, 1, 'ios', '6.0', 0, '1.0', 'ipad', to_date('2011-01-01', 'YYYY-MM-DD'), to_date('2015-01-01', 'YYYY-MM-DD'), sysdate , sysdate); 
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated) values(ccidist_seq_driver_info.nextval, 'R2-Gazette'    , 1, 1, 'ios', '6.0', 0, '1.0', 'ipad', to_date('2011-01-01', 'YYYY-MM-DD'), to_date('2015-01-01', 'YYYY-MM-DD'), sysdate , sysdate); 

insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'addressa', 0, 0, '', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'addressa', 0, 0, '', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'addressa', 0, 0, '', '', '1.0', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'addressa', 1024, 768, 'Android', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'addressa', 2048, 1546, 'Android', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'Chronicle' , 0, 0, '', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'DigitalNGToday', 0, 0, '', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'iPad-P', 0, 0, '', '', '1.1', 'ipad', sysdate, sysdate, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (ccidist_seq_matching_rules.nextval, 1, 'R2-Gazette', 0, 0, '', '', '1.1', 'ipad', sysdate, sysdate, 0);
