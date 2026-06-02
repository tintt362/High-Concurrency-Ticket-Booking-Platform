package com.xxxx.ddd.controller.model.enums;

import com.xxxx.ddd.controller.model.vo.ResultMessage;

/**
 * Tiện ích trả về kết quả
 *
 * @author vantrang
 */
public class ResultUtil<T> {

    /**
     * Lớp trừu tượng, lưu trữ kết quả
     */
    private final ResultMessage<T> responseMessage;
    /**
     * Phản hồi thành công
     */
    private static final Integer SUCCESS_CODE = 200;


    /**
     * Phương thức khởi tạo, đặt giá trị mặc định cho kết quả phản hồi
     */
    public ResultUtil() {
        responseMessage = new ResultMessage<>();
        responseMessage.setSuccess(true);
        responseMessage.setMessage("success");
        responseMessage.setCode(SUCCESS_CODE);
    }

    /**
     * Trả về dữ liệu
     *
     * @param t Kiểu dữ liệu
     * @return Thông báo
     */
    public ResultMessage<T> setData(T t) {
        this.responseMessage.setResult(t);
        return this.responseMessage;
    }


    /**
     * Trả về thông báo thành công
     *
     * @param resultCode Mã trả về
     * @return Trả về thông báo thành công
     */
    public ResultMessage<T> setSuccessMsg(ResultCode resultCode) {
        this.responseMessage.setSuccess(true);
        this.responseMessage.setMessage(resultCode.message());
        this.responseMessage.setCode(resultCode.code());
        return this.responseMessage;

    }

    /**
     * Phương thức tĩnh trừu tượng, trả về tập kết quả
     *
     * @param t   Kiểu dữ liệu
     * @param <T> Kiểu dữ liệu
     * @return Thông báo
     */
    public static <T> ResultMessage<T> data(T t) {
        return new ResultUtil<T>().setData(t);
    }

    /**
     * Trả về thành công
     *
     * @param responseStatusCode Mã trạng thái trả về
     * @return Thông báo
     */
    public static <T> ResultMessage<T> success(ResultCode responseStatusCode) {
        return new ResultUtil<T>().setSuccessMsg(responseStatusCode);
    }

    /**
     * Trả về thành công
     *
     * @return Thông báo
     */
    public static <T> ResultMessage<T> success() {
        return new ResultUtil<T>().setSuccessMsg(ResultCode.SUCCESS);
    }

    /**
     * Trả về thất bại
     *
     * @param responseStatusCode Mã trạng thái trả về
     * @return Thông báo
     */
    public static <T> ResultMessage<T> error(ResultCode responseStatusCode) {
        return new ResultUtil<T>().setErrorMsg(responseStatusCode);
    }

    /**
     * Trả về thất bại
     *
     * @param code Mã trạng thái
     * @param msg  Thông báo trả về
     * @return Thông báo
     */
    public static <T> ResultMessage<T> error(Integer code, String msg) {
        return new ResultUtil<T>().setErrorMsg(code, msg);
    }

    /**
     * Lỗi máy chủ, thêm mã trạng thái
     *
     * @param resultCode Mã trả về
     * @return Thông báo
     */
    public ResultMessage<T> setErrorMsg(ResultCode resultCode) {
        this.responseMessage.setSuccess(false);
        this.responseMessage.setMessage(resultCode.message());
        this.responseMessage.setCode(resultCode.code());
        return this.responseMessage;
    }

    /**
     * Lỗi máy chủ, thêm mã trạng thái
     *
     * @param code Mã trạng thái
     * @param msg  Thông báo trả về
     * @return Thông báo
     */
    public ResultMessage<T> setErrorMsg(Integer code, String msg) {
        this.responseMessage.setSuccess(false);
        this.responseMessage.setMessage(msg);
        this.responseMessage.setCode(code);
        return this.responseMessage;
    }
}
