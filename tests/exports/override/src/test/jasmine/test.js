describe("A suite", function() {
  it("contains spec with an expectation", function() {
    var test = new window.aaa.bbb.ccc.ddd.ZZZ();

    expect(test.test1('INSTANCE METHOD CALL')).toBe('INSTANCE METHOD CALL');
    expect(test.id).toBe('qwerty');

    expect(window.aaa.bbb.ccc.ddd.ZZZ.test2('STATIC METHOD CALL')).toBe('STATIC METHOD CALL');
    expect(window.aaa.bbb.ccc.ddd.ZZZ.staticProperty).toBe('staticProperty');
  });
});