package com.xxxx.ddd.controller.model.enums;

/**
 * Trả về mã trạng thái
 * Chữ số đầu tiên: 1: Sản phẩm; 2: Người dùng; 3: Giao dịch,
 * 4: Khuyến mãi, 5: Cửa hàng, 6: Trang web, 7: Cài đặt, 8: Khác
 *
 * @author vantrang
 */
public enum ResultCode {

    /**
     * Mã trạng thái thành công
     */
    SUCCESS(200, "Thành công"),

    /**
     * Tham số bất thường
     */
    PARAMS_ERROR(4002, "Tham số bất thường"),

    /**
     * Mã lỗi trả về
     */
    DEMO_SITE_FORBIDDEN_ERROR(4001, "Trang web demo không được phép sử dụng"),

    /**
     * Mã lỗi trả về
     */
    ERROR(400, "Máy chủ bận, vui lòng thử lại sau"),

    /**
     * Người dùng
     */
    USER_SESSION_EXPIRED(20004, "Phiên đăng nhập của người dùng đã hết hạn, vui lòng đăng nhập lại"),
    USER_PERMISSION_ERROR(20005, "Quyền hạn không đủ"),
    USER_PASSWORD_ERROR(20010, "Mật khẩu không chính xác"),
    USER_AUTH_ERROR(20005, "Quyền hạn không đủ"),
    EMPLOYEE_NOT_FOUND(20027, "Nhân viên không tồn tại"),
    EMPLOYEE_DISABLED(20031, "Nhân viên đã bị vô hiệu hóa"),
    USER_NOT_FOUND(20002, "Người dùng không tồn tại hoặc tài khoản đã bị vô hiệu hóa"),


    /**
     * Cửa hàng
     */

    STORE_NOT_FOUND(50001, "Không tìm thấy cửa hàng"),
    STORE_NAME_ALREADY_EXISTS(50002, "Tên cửa hàng đã tồn tại!"),
    STORE_ALREADY_HAS_STORE(50003, "Bạn đã sở hữu cửa hàng!"),
    STORE_NOT_OPENED(50004, "Thành viên này chưa mở cửa hàng"),
    STORE_NOT_LOGGED_IN(50005, "Chưa đăng nhập vào cửa hàng"),
    STORE_CLOSED(50006, "Cửa hàng đóng cửa, vui lòng liên hệ với quản trị viên"),
    STORE_DELIVER_PRODUCT_ADDRESS(50007, "Vui lòng điền địa chỉ giao hàng của nhà cung cấp"),
    FREIGHT_TEMPLATE_NOT_FOUND(50010, "Mẫu hiện tại không tồn tại"),
    STORE_STATUS_IN_PROGRESS(50011, "Cửa hàng đang trong quá trình đăng ký hoặc phê duyệt, vui lòng không thực hiện thao tác lặp lại"),
    STORE_SHIPPING_ADDRESS_REQUIRED(50012, "Vui lòng điền địa chỉ giao hàng"),


    /**
     * Sản phẩm
     */
    PRODUCT_ERROR(11001, "Sản phẩm lỗi, vui lòng thử lại sau"),
    PRODUCT_NOT_EXIST(11001, "Sản phẩm đã hết hàng"),
    PRODUCT_NAME_ERROR(11002, "Tên sản phẩm không chính xác, tên phải từ 2-50 ký tự"),
    PRODUCT_UNDER_ERROR(11003, "Xuống kệ sản phẩm thất bại"),
    PRODUCT_UPPER_ERROR(11004, "Lên kệ sản phẩm thất bại"),
    PRODUCT_AUTH_ERROR(11005, "Phê duyệt sản phẩm thất bại"),
    POINT_PRODUCT_ERROR(11006, "Giao dịch điểm tích lũy sản phẩm bất thường, vui lòng thử lại sau"),
    POINT_PRODUCT_NOT_EXIST(11020, "Sản phẩm điểm tích lũy không tồn tại"),
    POINT_PRODUCT_CATEGORY_EXIST(11021, "Danh mục sản phẩm điểm tích lũy hiện tại đã tồn tại"),
    PRODUCT_SKU_SN_ERROR(11007, "Mã SKU sản phẩm không được để trống"),
    PRODUCT_SKU_PRICE_ERROR(11008, "Giá sản phẩm SKU không được nhỏ hơn hoặc bằng 0"),
    PRODUCT_SKU_COST_ERROR(11009, "Giá vốn sản phẩm SKU không được nhỏ hơn hoặc bằng 0"),
    PRODUCT_SKU_WEIGHT_ERROR(11010, "Trọng lượng sản phẩm không được âm"),
    PRODUCT_SKU_QUANTITY_ERROR(11011, "Số lượng kho sản phẩm không được âm"),
    PRODUCT_SKU_QUANTITY_NOT_ENOUGH(11011, "Số lượng kho không đủ"),
    MUST_HAVE_PRODUCT_SKU(11012, "Phải có ít nhất một thông số kỹ thuật!"),
    MUST_HAVE_SALES_MODEL(11022, "Chế độ bán hàng là bán sỉ thì phải có quy tắc bán sỉ!"),

    HAVE_INVALID_SALES_MODEL(11023, "Quy tắc bán sỉ có dữ liệu không hợp lệ nhỏ hơn hoặc bằng 0!"),
    MUST_HAVE_PRODUCT_SKU_VALUE(11024, "Giá trị thông số kỹ thuật không được để trống!"),
    DO_NOT_MATCH_WHOLESALE(11025, "Số lượng mua sản phẩm bán sỉ không được thấp hơn số lượng khởi điểm!"),
    PRODUCT_NOT_ERROR(11026, "Sản phẩm không tồn tại"),

    PRODUCT_PARAMS_ERROR(11013, "Thông số kỹ thuật sản phẩm lỗi, vui lòng làm mới và thử lại"),
    PHYSICAL_PRODUCT_NEED_TEMP(11014, "Sản phẩm thực tế cần chọn mẫu vận chuyển"),
    VIRTUAL_PRODUCT_NOT_NEED_TEMP(11015, "Sản phẩm ảo không cần chọn mẫu vận chuyển"),
    PRODUCT_NOT_EXIST_STORE(11017, "Người dùng hiện tại không có quyền thao tác sản phẩm này"),
    PRODUCT_TYPE_ERROR(11016, "Cần chọn loại sản phẩm"),
    PRODUCT_STOCK_IMPORT_ERROR(11018, "Nhập kho sản phẩm thất bại, vui lòng kiểm tra dữ liệu trong bảng"),

    /**
     * Thương hiệu
     */
    PRODUCT_BRAND_SAVE_ERROR(14001, "Thêm thương hiệu thất bại"),
    PRODUCT_BRAND_UPDATE_ERROR(14002, "Cập nhật thương hiệu thất bại"),
    PRODUCT_BRAND_DISABLE_ERROR(14003, "Vô hiệu hóa thương hiệu thất bại"),
    PRODUCT_BRAND_DELETE_ERROR(14004, "Xóa thương hiệu thất bại"),
    PRODUCT_BRAND_NAME_EXIST_ERROR(20002, "Tên thương hiệu đã tồn tại!"),
    PRODUCT_BRAND_USE_DISABLE_ERROR(20003, "Danh mục đã được liên kết với thương hiệu, vui lòng hủy liên kết trước"),
    PRODUCT_BRAND_BIND_ERROR(20005, "Thương hiệu đã được liên kết với sản phẩm, vui lòng hủy liên kết trước"),
    PRODUCT_BRAND_NOT_EXIST(20004, "Thương hiệu không tồn tại"),

    /**
     * Thông số kỹ thuật
     */
    PRODUCT_SPEC_SAVE_ERROR(13001, "Lưu thông số kỹ thuật thất bại"),
    PRODUCT_SPEC_UPDATE_ERROR(13002, "Cập nhật thông số kỹ thuật thất bại"),
    PRODUCT_SPEC_DELETE_ERROR(13003, "Phân loại đã liên kết với thông số kỹ thuật này, vui lòng hủy liên kết trước"),

    /**
     * Danh mục
     */
    PRODUCT_CATEGORY_NOT_EXIST(10001, "Danh mục sản phẩm không tồn tại"),
    PRODUCT_CATEGORY_NAME_IS_EXIST(10002, "Tên danh mục này đã tồn tại"),
    PRODUCT_CATEGORY_PARENT_NOT_EXIST(10003, "Tên danh mục này đã tồn tại"),
    PRODUCT_CATEGORY_BEYOND_THREE(10004, "Tối đa là 3 cấp danh mục, thêm thất bại"),
    PRODUCT_CATEGORY_HAS_CHILDREN(10005, "Danh mục này có chứa danh mục con, không thể xóa"),
    CATEGORY_HAS_PRODUCT(10006, "Danh mục này có chứa sản phẩm, không thể xóa"),
    PRODUCT_CATEGORY_SAVE_ERROR(10007, "Danh mục này có chứa sản phẩm, không thể xóa"),
    PRODUCT_CATEGORY_PARAMETER_NOT_EXIST(10012, "Nhóm tham số liên kết với danh mục không tồn tại"),
    PRODUCT_CATEGORY_PARAMETER_SAVE_ERROR(10008, "Thêm nhóm tham số liên kết với danh mục thất bại"),
    PRODUCT_CATEGORY_PARAMETER_UPDATE_ERROR(10009, "Thêm nhóm tham số liên kết với danh mục thất bại"),
    PRODUCT_CATEGORY_DELETE_FLAG_ERROR(10010, "Trạng thái của danh mục con không thể khác với trạng thái của danh mục cha!"),
    PRODUCT_CATEGORY_COMMISSION_RATE_ERROR(10011, "Tỷ lệ hoa hồng của danh mục không chính xác!"),


    /**
     * Tham số
     */
    PRODUCT_PARAMETER_SAVE_ERROR(12001, "Thêm tham số thất bại"),
    PRODUCT_PARAMETER_UPDATE_ERROR(12002, "Chỉnh sửa tham số thất bại"),

    /**
     * Ngoại lệ hệ thống
     */
    RATE_LIMIT_ERROR(1003, "Truy cập quá thường xuyên, vui lòng thử lại sau"),
    ;


    private final Integer code;
    private final String message;


    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}
