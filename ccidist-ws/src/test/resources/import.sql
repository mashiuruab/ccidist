/*This is only valid for mysql, hsql and this file import.sql is used for integration tests*/
insert into organization (id, name, created, updated, version) values ('polaris', 'Polaris', current_timestamp, current_timestamp, 0);
insert into organization (id, name, created, updated, version) values ('nhst', 'NHST', current_timestamp, current_timestamp, 0);
insert into organization (id, name, created, updated, version) values ('axelspringer', 'AxelSpringer', current_timestamp, current_timestamp, 0);

insert into publication (id, name, organization_id, created, updated, version) values ('addressa', 'Addressa', 'polaris', current_timestamp, current_timestamp, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('harstadtidende', 'Harstadtidende', 'polaris', current_timestamp, current_timestamp, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('nhst-sports', 'NHST-SPORTS', 'nhst', current_timestamp, current_timestamp, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('nhst-news', 'NHST-NEWS', 'nhst', current_timestamp, current_timestamp, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('axelSpringer-sports', 'AxelSpringer-SPORTS', 'axelspringer', current_timestamp, current_timestamp, 0);
insert into publication (id, name, organization_id, created, updated, version) values ('axelSpringer-entertainment', 'AxelSpringer-ENTERTAINMENT', 'axelspringer', current_timestamp, current_timestamp, 0);

insert into users (name, login_name, password, created, updated, user_privilege_id, version) values ('Administrative User', 'admin', '2d8cc94a8c8b5ca7400969c5b2e572c1', current_timestamp, current_timestamp, null, 0);
insert into role (id, name, version) values (1, 'Portal', 0);
insert into role (id, name, version) values (2, 'Ingester', 0);

insert into design_to_epub_mapper (id, design_name, epub_name, version) values (1, 'ipad', 'ipad2', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (2, 'ipad', 'ipad3', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (3, 'ipad-mini', 'ipad-mini-1', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (4, 'ipad', 'ipad-mini', 0);
insert into design_to_epub_mapper (id, design_name, epub_name, version) values (5, 'ipad', 'ipad4', 0);

insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated, internal) values(1, 'addressa', 1, 1, 'ios', '6.0', 0, '1.0', 'ipad2', CURRENT_TIMESTAMP , DATE_ADD(curdate(), INTERVAL 1 DAY), CURRENT_TIMESTAMP , CURRENT_TIMESTAMP, false);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated, internal) values(2, 'addressa', 2, 1, 'ios', '6.0', 0, '1.0', 'ipad3', CURRENT_TIMESTAMP , DATE_ADD(curdate(), INTERVAL 1 DAY), CURRENT_TIMESTAMP , CURRENT_TIMESTAMP, false);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated, internal) values(3, 'addressa', 3, 1, 'ios', '6.0', 0, '2.0', 'ipad-mini', CURRENT_TIMESTAMP , DATE_ADD(curdate(), INTERVAL 1 DAY), CURRENT_TIMESTAMP , CURRENT_TIMESTAMP, false);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated, internal) values(4, 'addressa', 4, 1, 'ios', '6.0', 0, '2.0', 'ipad-mini', CURRENT_TIMESTAMP , DATE_ADD(curdate(), INTERVAL 1 DAY), CURRENT_TIMESTAMP , CURRENT_TIMESTAMP, false);
insert into driver_info(id, publication_id, design_to_epub_mapper_id, pre_generate, os, os_version, version, reader, device_name, start_date, end_date, created, updated, internal) values(5, 'addressa', 5, 0, 'ios', '6.0', 0, '2.0', 'ipad4', CURRENT_TIMESTAMP , DATE_ADD(curdate(), INTERVAL 1 DAY), CURRENT_TIMESTAMP , CURRENT_TIMESTAMP, false);

insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (1, 1, 'addressa', 0, 0, '', '', '1.1', 'ipad', current_timestamp, current_timestamp, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (2, 1, 'addressa', 0, 0, '', '', '1.1', 'ipad', current_timestamp, current_timestamp, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (3, 1, 'addressa', 0, 0, '', '', '1.0', 'ipad', current_timestamp, current_timestamp, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (4, 1, 'addressa', 1024, 768, 'Android', '', '1.1', 'ipad', current_timestamp, current_timestamp, 0);
insert into matching_rules (id, design_to_epub_mapper_id, publication_id, width, height, os, osv, reader_version, device_name, created, updated, version) values (5, 1, 'addressa', 2048, 1546, 'Android', '', '1.1', 'ipad', current_timestamp, current_timestamp, 0);
