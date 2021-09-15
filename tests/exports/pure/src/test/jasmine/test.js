describe("A suite", function() {
  it("contains spec with an expectation", function() {
    var test = new org.treblereel.j2cl.exports.ExportTestClass();

    expect(test.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    expect(test.id).toBe('qwerty');

    expect(org.treblereel.j2cl.exports.ExportTestClass.test2('STATIC METHOD CALL')).toBe('STATIC METHOD CALL');
    expect(org.treblereel.j2cl.exports.ExportTestClass.staticProperty).toBe('staticProperty');

    var test2 = new org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass();

    expect(test2.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    expect(test2.id).toBe('qwerty');

    expect(org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass.test2('STATIC METHOD CALL')).toBe('STATIC METHOD CALL');
    expect(org.treblereel.j2cl.exports.A_B_C_D_ExportTestClass.staticProperty).toBe('staticProperty');

    var test3 = new org.treblereel.j2cl.exports.SimpleTestClass();

    expect(test3.staticValue).toBe('staticValue');
    expect(test3.staticValueJsProperty).toBe('staticValueJsProperty');

    expect(test3.instanceValue).toBe('instanceValue');
    expect(test3.instanceValueJsProperty).toBe('instanceValueJsProperty');


    expect(test3.test2('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    test3.setValue('SimpleTestClass');
    expect(test3.getValue()).toBe('SimpleTestClass');

    var test4 = new org.treblereel.j2cl.exports.SimpleTestJsTypeClass();

    expect(test4.test2('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    test4.setValue('SimpleTestJsTypeClass');
    expect(test4.getValue()).toBe('SimpleTestJsTypeClass');

    var test5 = new org.treblereel.j2cl.exports.SimpleTestJsConstructorClass();

    expect(test5.test2('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    test5.setValue('SimpleTestJsConstructorClass');
    expect(test5.getValue()).toBe('SimpleTestJsConstructorClass');

  });
});